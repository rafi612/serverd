package com.serverd.main;

import java.io.File;

import com.serverd.app.ServerdApplication;
import com.serverd.client.ClientManager;
import com.serverd.config.Config;
import com.serverd.log.Log;
import com.serverd.plugin.PluginLoadException;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.PluginUtils;
import com.serverd.server.ServerManager;

public class Main {
	public static final String VERSION = "v1.2.0";
	
	public static String workingDir = ServerdApplication.getWorkDir("serverd");
	
	public static void main(String[] args) {
		Log log = new Log("ServerD");
		
		boolean plugins = true;
		boolean isLoadingApp = false;
		String appClass = ""; 
		
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
		File workdirFile = new File(workingDir);
		if (!workdirFile.exists())
			if (!workdirFile.mkdir()) {
				log.error("Failed to create working directory in " + workingDir);
				System.exit(1);
			}
		
		//load config
		Config config = null;
		try {
			File configFile = new File(workdirFile,"config.properties");
			Config.createIfNotExists(configFile, new Config(), "Default ServerD config file");
			config = Config.load(configFile, Config.class);
		} catch (Exception e) {
			log.error("Error: " + e.getMessage());
			System.exit(1);
		}
		
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
					appClass = args[i + 1];
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
		
		System.out.println("ServerD " + VERSION);
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			ServerManager.shutdown();
			ClientManager.shutdown();
		}));
		
		ServerManager.addDefaultServers(config);

		try {
			PluginManager.init(workdirFile);
			if (plugins) {
				log.info("Loading plugins...");
				PluginManager.loadPlugins();
			}
		} catch (Exception e) {
			log.error("Error: " + e.getMessage());
			System.exit(-1);
		}
		
		if (isLoadingApp) {
			log.info("Loading app " + appClass + "...");
			try {
				PluginUtils.loadPluginAsApp(appClass);
			} catch (PluginLoadException e) {
				if (e.getCause() instanceof ClassNotFoundException) {
					System.err.println("Class " + appClass + " not found");
					System.exit(1);
				} else System.err.println("App load error:" + e.getMessage());
			}
		}
		
		log.info("Starting servers...");
		ServerManager.init();
	}
}
