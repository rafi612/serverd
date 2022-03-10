package com.serverd.main;

import com.serverd.client.ClientManager;
import com.serverd.log.Log;
import com.serverd.plugin.Debug;
import com.serverd.plugin.PluginManager;

public class Main
{
	public static final String VERSION = "v1.0.2";
	public static void main(String[] args)
	{
		boolean plugins = true;
		boolean pluginDebug = false;
		String pluginDebugClass = ""; 
		
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
				case "--plugin-loc":
					if (i + 1 > args.length)
					{
						System.err.println("--plugin-loc: missing argument");
						break;
					}
					PluginManager.plugindir = args[i + 1];
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
			Log.log("ServerD", "Loading plugins...");
			PluginManager.loadPlugins();
		}
		
		if (pluginDebug)
		{
			Log.log("ServerD", "Loading debug plugin " + pluginDebugClass + "...");
			try {
				Debug.loadPluginFromClassName(pluginDebugClass);
			} catch (ClassNotFoundException e) {
				System.err.println("Class " + pluginDebugClass + " not found");
				System.exit(-1);
			} catch (Exception e) {
				System.err.println("Debug plugin load error:" + e.getMessage());
			}
		}
		
		Log.log("ServerD","Starting listening clients...");
		ClientManager.start("0.0.0.0",9999,9998);

	}	

}