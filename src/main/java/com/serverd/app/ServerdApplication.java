package com.serverd.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

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
	private final Log log = new Log("ServerD");
	public static final String SERVERD_VERSION = "v1.0.0";
	private final String name;
	private final ClientManager clientManager;
	private final ServerManager serverManager;
	private final PluginManager pluginManager;
	private String splash = "ServerD " + SERVERD_VERSION;
	private File workdir;
	private Config config;
	private boolean plugins;
	private boolean isLoadingApp;
	private String appClassName;
	private boolean wasInitialized = false;

	private final DirectorySchema directorySchema;

	public ServerdApplication() {
		this("serverd");
	}

	public ServerdApplication(String name) {
		this(name,new DirectorySchema());
	}

	public ServerdApplication(String name, DirectorySchema directorySchema) {
		this.name = name;

		clientManager = new ClientManager(this);
		serverManager = new ServerManager(this);
		pluginManager = new PluginManager(this);

		this.directorySchema = directorySchema;
	}

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
			if (plugins) {
				log.info("Loading plugins...");
				pluginManager.loadPlugins();
			}
		} catch (Exception e) {
			log.error("Error: " + e.getMessage());
			System.exit(-1);
		}

		if (isLoadingApp) {
			log.info("Loading app " + appClassName + "...");
			try {
				PluginUtils.loadPluginAsApp(appClassName, pluginManager);
			} catch (PluginLoadException e) {
				if (e.getCause() instanceof ClassNotFoundException) {
					log.error("Class " + appClassName + " not found");
					System.exit(1);
				} else log.error("App load error:" + e.getMessage());
			}
		}

		wasInitialized = true;
	}

	public void run() {
		if (!wasInitialized)
			init();

		log.info("Starting servers...");
		serverManager.init();
	}

	public void createWorkDir() {
		if (workdir == null)
			setWorkdir(getDefaultWorkDir());

		if (!workdir.exists())
			if (!workdir.mkdir()) {
				log.error("Failed to create working directory in " + workdir.getAbsolutePath());
				System.exit(1);
			}
	}

	public Config loadConfig() {
		try {
			File configFile = new File(directorySchema.get(workdir,DirectorySchema.SERVERD_ROOT_DIR),"config.properties");
			Config.createIfNotExists(configFile, new Config(), "Default ServerD config file");
			return Config.load(configFile, Config.class);
		} catch (Exception e) {
			log.error("Error: " + e.getMessage());
			System.exit(1);
		}
        return null;
    }

	public File getDefaultWorkDir() {
		return new File(ServerdApplication.getWorkDir(name));
	}

	public void loadPluginAppFromName(String className) {
		isLoadingApp = true;
		this.appClassName = className;
	}

	public void loadPluginApp(Class<?> appClass) {
		isLoadingApp = true;
		this.appClassName = appClass.getName();
	}

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

		//parse other arguments
		for (int i = 0;i < args.length;i++)
			if(args[i].startsWith("--")) {
				switch(args[i]) {
					case "--noplugins":
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

		setConfig(config);

		loadPlugins(plugins);

		if (isLoadingApp())
			loadPluginAppFromName(appClassName);
	}

	public void loadPlugins(boolean b) {
		plugins = b;
	}

	public String getSplash() {
		return splash;
	}

	public void setSplash(String splash) {
		this.splash = splash;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public File getWorkdir() {
		return workdir;
	}

	public void setWorkdir(File workdir) {
		this.workdir = workdir;
	}

	public boolean isLoadingApp() {
		return isLoadingApp;
	}

	public ClientManager getClientManager() {
		return clientManager;
	}

	public ServerManager getServerManager() {
		return serverManager;
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public DirectorySchema getDirectorySchema() {
		return directorySchema;
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
	 * @return Working directory path.
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
