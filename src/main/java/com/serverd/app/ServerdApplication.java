package com.serverd.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.serverd.client.ClientManager;
import com.serverd.config.Config;
import com.serverd.log.Log;
import com.serverd.plugin.PluginLoadException;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.PluginUtils;
import com.serverd.server.ServerManager;

/**
 * Allows to run ServerD inside plugin to create self-contained app.
 */
public class ServerdApplication {
	/** ServerD version */
	public static final String SERVERD_VERSION = "v1.0.0";

	private final Log log = Log.get(ServerdApplication.class);
	private final String name;
	private final ClientManager clientManager;
	private final ServerManager serverManager;
	private final PluginManager pluginManager;
	private String splash = "ServerD " + SERVERD_VERSION;
	private File workdir;
	private Config config;
	private boolean plugins = true;
	private boolean isLoadingApp;
	private String appClassName;
	private boolean wasInitialized = false;
	private final DirectorySchema directorySchema;
	private final ArrayList<String> pluginsList = new ArrayList<>();

	/**
	 * Constructor setting default app name to "serverd".
	 */
	public ServerdApplication() {
		this("serverd");
	}

	/**
	 * Constructor setting custom app name and default directory schema.
	 * @param name App name.
	 */
	public ServerdApplication(String name) {
		this(name,new DirectorySchema());
	}

	/**
	 * Constructor setting custom application name and custom directory schema.
	 * @param name App name.
	 * @param directorySchema Directory schema.
	 */
	public ServerdApplication(String name, DirectorySchema directorySchema) {
		this.name = name;

		this.clientManager = new ClientManager(this);
		this.serverManager = new ServerManager(this);
		this.pluginManager = new PluginManager(this);

		this.directorySchema = directorySchema;
	}

	/**
	 * Initializing application.
	 * Invoked automatically when not invoked before {@link ServerdApplication#run()}.
	 */
	public void init() {
		try {
			System.out.println(splash);

			createWorkDir();
			directorySchema.init(workdir);

			if (config == null)
				config = loadConfig();

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				serverManager.shutdown();
				clientManager.shutdown();
				pluginManager.shutdown();
			}));

			serverManager.addDefaultServers(clientManager,config);
			pluginManager.init(workdir,directorySchema);

			wasInitialized = true;
		} catch (Exception e) {
			log.error("Error: " + e.getMessage());
			throwAppError(e);
		}
	}

	/**
	 * Starting application.
	 * Invoking {@link ServerdApplication#init()} automatically when not initialized before.
	 */
	public void run() {
		if (!wasInitialized)
			init();

		try {
			if (plugins) {
				log.info("Loading plugins...");

				for (String pluginClass : pluginsList) {
					log.info("Loading plugin class " + pluginClass);
					PluginUtils.loadPluginFromClassName(pluginClass,pluginManager);
				}

				pluginManager.loadPlugins();
			}
		} catch (Exception e) {
			log.error("Error: " + e.getMessage());
			throwAppError(e);
		}

		if (isLoadingApp) {
			log.info("Loading app " + appClassName + "...");
			try {
				PluginUtils.loadPluginAsApp(appClassName, pluginManager);
			} catch (PluginLoadException e) {
				if (e.getCause() instanceof ClassNotFoundException) {
					log.error("Class " + appClassName + " not found");
					throwAppError(e);
				} else log.error("App load error:" + e.getMessage());
			}
		}

		log.info("Starting servers...");
		serverManager.init();
	}

	private void createWorkDir() {
		if (workdir == null)
			setWorkdir(getDefaultWorkDir());

		if (!workdir.exists())
			if (!workdir.mkdir()) {
				log.error("Failed to create working directory in " + workdir.getAbsolutePath());
				throwAppError(new IOException());
			}
	}

	private Config loadConfig() {
		try {
			File configFile = new File(directorySchema.get(workdir,DirectorySchema.SERVERD_ROOT_DIR),"config.properties");
			Config.createIfNotExists(configFile, new Config(), "Default ServerD config file");
			return Config.load(configFile, Config.class);
		} catch (Exception e) {
			log.error("Error: " + e.getMessage());
			throwAppError(e);
		}
        return null;
    }

	/**
	 * Returns default working directory for application.
	 */
	public File getDefaultWorkDir() {
		return new File(ServerdApplication.getWorkDir(name));
	}

	/**
	 * Loading plugin as main for application by class name.
	 * @param className Plugin class name.
	 */
	public void loadPluginAppFromName(String className) {
		isLoadingApp = true;
		this.appClassName = className;
	}

	/**
	 * Loading plugin as main for application by class object.
	 * @param appClass Plugin class object.
	 */
	public void loadPluginApp(Class<?> appClass) {
		isLoadingApp = true;
		this.appClassName = appClass.getName();
	}

	/**
	 * Parsing command line argument.
	 * Can be used if we want to control application using some standard parameters.
	 * <p> Command line parameters: </p>
	 * <p> --working-loc - setting working location of app. </p>
	 * <p> --no-plugins - disabling plugins in app. </p>
	 * <p> --ip - setting IP for all servers. </p>
	 * <p> --tcp-port - setting port for TCP Server. </p>
	 * <p> --udp-port - setting port for UDP Server. </p>
	 *
	 * @param args Command line arguments.
	 */
	public void parseCmdArgs(String[] args) {
		String workingDir = getDefaultWorkDir().getAbsolutePath();

		//parse work dir argument
		for (int i = 0;i < args.length;i++)
			if(args[i].equals("--working-loc")) {
				if (i + 1 > args.length) {
					System.err.println("--working-loc: missing argument");
					break;
				}
				workingDir = args[i + 1];
			}

		//create work dir
		setWorkdir(new File(workingDir));
		createWorkDir();

		try {
			directorySchema.init(workdir);
		} catch (IOException e) {
			log.error("Error while creating directories: " + e.getMessage());
		}

		//load config
		Config config = loadConfig();
		if (config != null) {
			//parse other arguments
			for (int i = 0;i < args.length;i++)
				if(args[i].startsWith("--")) {
					switch(args[i]) {
						case "--no-plugins":
							plugins = false;
							break;
						case "--app-class":
							if (i + 1 > args.length) {
								System.err.println("--app-class: missing argument");
								break;
							}
							isLoadingApp = true;
							appClassName = args[i + 1];
							break;
						case "--ip":
							if (i + 1 > args.length) {
								System.err.println("--ip: missing argument");
								break;
							}
							config.ip = args[i + 1];
							break;
						case "--tcp-port":
							if (i + 1 > args.length) {
								System.err.println("--tcp-port: missing argument");
								break;
							}
							config.tcpPort = Integer.parseInt(args[i + 1]);
							break;
						case "--udp-port":
							if (i + 1 > args.length) {
								System.err.println("--udp-port: missing argument");
								break;
							}
							config.udpPort = Integer.parseInt(args[i + 1]);
							break;
						case "--property":
							if (i + 2 > args.length) {
								System.err.println("--property: missing argument");
								break;
							}
							System.setProperty(args[i + 1], args[i + 2]);
							break;
					}
				}
		} else throwAppError("Config load failed");

		setConfig(config);

		loadPlugins(plugins);

		if (isLoadingApp())
			loadPluginAppFromName(appClassName);
	}

	/**
	 * Loading plugin from given class name.
	 * @param className Class name.
	 */
	public void loadPlugin(String className) {
		pluginsList.add(className);
	}

	/**
	 * Loading plugin from given class object.
	 * @param clazz Class object.
	 */
	public void loadPlugin(Class<?> clazz) {
		loadPlugin(clazz.getName());
	}

	/**
	 * Setting if app loads external plugins. Plugins loading are enabled by default.
	 * @param wantPlugins true if you want load plugins.
	 */
	public void loadPlugins(boolean wantPlugins) {
		plugins = wantPlugins;
	}

	/**
	 * Returns app splash string displayed on start of application.
	 */
	public String getSplash() {
		return splash;
	}

	/**
	 * Setting application splash string displayed on start of application.
	 * @param splash Splash string.
	 */
	public void setSplash(String splash) {
		this.splash = splash;
	}

	/**
	 * Returns config object of application.
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * Setting config for application.
	 * @param config Config object.
	 */
	public void setConfig(Config config) {
		this.config = config;
	}

	/**
	 * Returns application working directory.
	 */
	public File getWorkdir() {
		return workdir;
	}

	/**
	 * Setting application working directory.
	 * @param workdir {@link File} object for working directory.
	 */
	public void setWorkdir(File workdir) {
		this.workdir = workdir;
	}

	/**
	 * Check if application loading plugins in application mode.
	 * @return true if application loading plugins in application mode.
	 */
	public boolean isLoadingApp() {
		return isLoadingApp;
	}

	/**
	 * Returns {@link ClientManager} of application.
	 */
	public ClientManager getClientManager() {
		return clientManager;
	}

	/**
	 * Returns {@link ServerManager} of application.
	 */
	public ServerManager getServerManager() {
		return serverManager;
	}

	/**
	 * Returns {@link PluginManager} of application.
	 */
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	/**
	 * Returns {@link DirectorySchema} of application.
	 */
	public DirectorySchema getDirectorySchema() {
		return directorySchema;
	}

	private void throwAppError(String message) {
		log.error(message);
		throw new RuntimeException("ServerD error: " + message);
	}

	private void throwAppError(Exception e) {
		throw new RuntimeException("ServerD error: " + e.getMessage());
	}

	/**
	 * Running ServerD and loading class as plugin to create self-contained app.
	 * @param appClass App class object.
	 */
	public static void run(Class<?> appClass) {
		run(appClass,new String[] {});
	}
	
	/**
	 * Running ServerD and loading class as plugin to create self-contained app.
	 * @param appClass App class object.
	 * @param args Command line arguments.
	 */
	public static void run(Class<?> appClass,String[] args) {
		run(appClass,args,false);
	}
	
	/**
	 * Running ServerD and loading class as plugin to create self-contained app.
	 * @param appClass App class object.
	 * @param args Command line arguments.
	 * @param otherPlugins Want to load another plugins?
	 */
	public static void run(Class<?> appClass,String[] args,boolean otherPlugins) {
		run(appClass,args,otherPlugins,getWorkDir("serverd"));
	}

	/**
	 * Running ServerD and loading class as plugin to create self-contained app.
	 * @param appClass App class object.
	 * @param args Command line arguments.
	 * @param otherPlugins Want to load another plugins?
	 * @param workdir Working directory path.
	 */
	public static void run(Class<?> appClass,String[] args,boolean otherPlugins,String workdir) {
		ServerdApplication app = new ServerdApplication();
		app.parseCmdArgs(args);
		app.loadPlugins(otherPlugins);
		app.setWorkdir(new File(workdir));
		app.loadPluginApp(appClass);
		app.run();
	}
	
	/**
	 * Returns default working directory.
	 */
	public static String getWorkDir(String name) {
		String osName = System.getProperty("os.name").toLowerCase();
		String userHome = System.getProperty("user.home");
		
		if (osName.startsWith("windows"))
			return Paths.get(System.getenv("APPDATA"),name).toString();
		else if (osName.contains("nux") || osName.contains("freebsd"))
			return Paths.get(userHome,".config",name).toString();
		else if (osName.contains("mac") || osName.contains("darwin"))
			return Paths.get(userHome,"Library","Application Support",name).toString();
		return userHome;
	}
}
