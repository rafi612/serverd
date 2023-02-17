package com.serverd.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import com.serverd.client.ClientManager;
import com.serverd.command.Commands;
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
		
		Runtime.getRuntime().addShutdownHook(new Thread(ClientManager::shutdown));
		
		File workdirFile = new File(workingdir);
		if (!workdirFile.exists())
			if (!workdirFile.mkdir())
			{
				log.error("Failed to create working directory in " + workingdir);
				System.exit(-1);
			}
		
		Commands.init();
		try 
		{
			PluginManager.init();
			if (plugins) 
			{
				log.info("Loading plugins...");
				PluginManager.loadPlugins();
			}
		} 
		catch (IOException e)
		{
			log.error("Error: " + e.getMessage());
			System.exit(-1);
		}
		
		if (pluginDebug)
		{
			log.info("Loading debug plugin " + pluginDebugClass + "...");
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
		
		log.info("Starting listening clients...");
		ClientManager.start(ip,tcp_port,udp_port);
	}
	
	/**
	 * Returns default working directory
	 * @return Working directory path.
	 */
	public static String getWorkDir()
	{
		String osname = System.getProperty("os.name").toLowerCase();
		String userhome = System.getProperty("user.home");
		
		if (osname.startsWith("windows"))
			return Paths.get(System.getenv("APPDATA"),"serverd").toString();
		else if (osname.contains("nux") || osname.contains("freebsd"))
			return Paths.get(userhome,".config","serverd").toString();
		else if (osname.contains("mac") || osname.contains("darwin"))
			return Paths.get(userhome,"Library","Application Support","serverd").toString();
		return userhome;
	}
}
