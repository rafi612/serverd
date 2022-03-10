package com.serverd.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import com.serverd.log.Log;
import com.serverd.plugin.command.Command;
import com.serverd.plugin.listener.ConnectListener;
import com.serverd.plugin.listener.UpdateIDListener;

public class PluginManager 
{
	public static String plugindir = getPluginDir();
	public static ArrayList<Plugin> plugins = new ArrayList<Plugin>();
	
	public static int plugins_loaded = 0;
	
//	public static ArrayList<ConnectListener> connectlisteners = new ArrayList<ConnectListener>();
//	public static ArrayList<UpdateIDListener> updateidlisteners = new ArrayList<UpdateIDListener>();
//	public static ArrayList<Command> commands = new ArrayList<Command>();

	public static void loadPlugins()
	{
		//create plugin dir
		File pdir = new File(plugindir);
		if (!pdir.exists())
			pdir.mkdirs();
		
		File[] files = pdir.listFiles();
		
		for (File f : files)
		{
			String message = load(f);
			
			if (!message.equals(""))
				Log.log("Plugin Manager", message);
		}
		
	}
	
	public static String load(File file)
	{
		Log.log("Plugin Manager","Loading plugin " + file.getName());
		
		try 
		{
			URLClassLoader child = new URLClassLoader(
			        new URL[] {file.toURI().toURL()}
			);
			
			String classname = file.getName().replace(".jar", "");
			
			String classpath = "com." + classname.toLowerCase() + "." + classname;
			
			Class<?> classToLoad = Class.forName(classpath, true, child);

			ServerdPlugin instance = (ServerdPlugin) classToLoad.getDeclaredConstructor().newInstance();
			Plugin plugin = new Plugin(file,instance);
			
			plugin.start();
			
			plugins_loaded++;
			
			plugins.add(plugin);
		} 
		catch (ClassNotFoundException e)
		{
			return file.getName() + ": Plugin Main class not found";
		}
		catch (NoClassDefFoundError e)
		{
			return file.getName() + ": Plugin load failed, can't load class: " + e.getMessage();
		}
		catch (Exception e)
		{
			return file.getName() + ": Plugin load failed: " + e.getMessage();
		}
		
		return "";
	}
	
	public static void addPlugin(Plugin p)
	{
		plugins.add(p);
	}
	
	private static String path(String... path)
	{
		String p = "";
		
		for (String s : path) p += s + File.separator;
		
		return p;
	}
	
	public static String[] listPluginsName()
	{
		String[] s = new String[plugins.size()];
		
		for (int i = 0;i < s.length;i++)
		{
			s[i] = plugins.get(i).file.getName();
		}
		
		return s;
	}
	
	public static Plugin getByFileName(String name)
	{
		for (Plugin p : plugins)
			if (p.file.getName().equals(name)) return p;
		return null;
	}
	
	public static String getPluginDir()
	{
		if (System.getProperty("os.name").startsWith("Windows"))
			return path(System.getenv("APPDATA"),"serverd","plugins").toString();
		
		else if (System.getProperty("os.name").contains("nux") || System.getProperty("os.name").contains("mac") )
			return path(System.getProperty("user.home"),".config","serverd","plugins").toString();
		return "";
	}

}
