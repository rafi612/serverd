package com.serverd.main;

import java.nio.file.Paths;

import com.serverd.client.ClientManager;
import com.serverd.log.Log;
import com.serverd.plugin.Debug;
import com.serverd.plugin.PluginManager;

public class Main
{
	public static final String VERSION = "v1.2.0";
	
	public static String workingdir = getWorkDir();
	
	public static void main(String[] args)
	{
		Log log = new Log("ServerD");
		
		boolean plugins = true;
		boolean pluginDebug = false;
		String pluginDebugClass = ""; 
		
		String ip = "0.0.0.0";
		int tcp_port = 9999,udp_port = 9998;
		
		for (int i = 0;i < args.length;i++)
			if(args[i].startsWith("--"))
		{
				switch(args[i])
				{
				case "--noplugins":
					plugins = false;
					break;
				case "--plugin-debug":
					if (i + 1 > args.length)
					{
						System.err.println("--plugin-debug: missing argument");
						break;
					}
					pluginDebug = true;
					pluginDebugClass = args[i + 1];
					break;
				case "--working-loc":
					if (i + 1 > args.length)
					{
						System.err.println("--working-loc: missing argument");
						break;
					}
					workingdir = args[i + 1];
					break;
				case "--ip":
					if (i + 1 > args.length)
					{
						System.err.println("--ip: missing argument");
						break;
					}
					ip = args[i + 1];
					break;
				case "--tcp-port":
					if (i + 1 > args.length)
					{
						System.err.println("--tcp-port: missing argument");
						break;
					}
					tcp_port = Integer.parseInt(args[i + 1]);
					break;
				case "--udp-port":
					if (i + 1 > args.length)
					{
						System.err.println("--udp-port: missing argument");
						break;
					}
					udp_port = Integer.parseInt(args[i + 1]);
					break;
				case "--property":
					if (i + 2 > args.length)
					{
						System.err.println("--property: missing argument");
						break;
					}
					System.setProperty(args[i + 1], args[i + 2]);
					break;
				}
		}
		
		System.out.println("ServerD " + VERSION);
		
		if (plugins) 
		{
			log.log("Loading plugins...");
			PluginManager.loadPlugins();
		}
		
		if (pluginDebug)
		{
			log.log("Loading debug plugin " + pluginDebugClass + "...");
			try 
			{
				Debug.loadPluginFromClassName(pluginDebugClass);
			} 
			catch (ClassNotFoundException e)
			{
				System.err.println("Class " + pluginDebugClass + " not found");
				System.exit(-1);
			} 
			catch (Exception e)
			{
				System.err.println("Debug plugin load error:" + e.getMessage());
			}
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(ClientManager::shutdown));
		
		log.log("Starting listening clients...");
		ClientManager.start(ip,tcp_port,udp_port);

	}
	public static String getWorkDir()
	{
		String osname = System.getProperty("os.name");
		if (osname.startsWith("Windows"))
			return Paths.get(System.getenv("APPDATA"),"serverd").toString();
		else if (osname.contains("nux") || osname.contains("freebsd"))
			return Paths.get(System.getProperty("user.home"),".config","serverd").toString();
		else if (osname.contains("mac") || osname.contains("darwin"))
			return Paths.get(System.getProperty("user.home"),"Library","Application Support","serverd").toString();
		return "";
	}

}
