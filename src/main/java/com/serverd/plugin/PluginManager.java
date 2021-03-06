package com.serverd.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.serverd.log.Log;
import com.serverd.main.Main;

/**
 * Plugin manager
 */
public class PluginManager 
{
	public static String plugindir = Paths.get(Main.workingdir,"plugins").toString();
	public static String plugindatadir = Paths.get(Main.workingdir,"pluginsdata").toString();
	
	public static File pluginsdisabled_file = Paths.get(Main.workingdir,"plugins_disabled.conf").toFile();
	
	public static List<String> pluginsdisabled;
	
	public static ArrayList<Plugin> plugins = new ArrayList<Plugin>();
	
	public static int plugins_loaded = 0;

	static Log log = new Log("Plugin Manager");

	/**
	 * Loading all plugins
	 */
	public static void loadPlugins()
	{
		//create plugin dir
		File pdir = new File(plugindir);
		if (!pdir.exists())
			pdir.mkdirs();
		
		File pdatadir = new File(plugindatadir);
		if (!pdatadir.exists())
			pdatadir.mkdirs();
		
		if (!pluginsdisabled_file.exists())
			try {
				pluginsdisabled_file.createNewFile();
			} catch (IOException e) {
				log.log("Error creating file:" + e.getMessage());
			}
		
		try {
			pluginsdisabled = Files.readAllLines(pluginsdisabled_file.toPath(), Charset.defaultCharset());
		} catch (IOException e) {
			log.log("Error reading file:" + e.getMessage());
		}
		
		File[] files = pdir.listFiles();
		
		for (File f : files)
		{
			String message = "";
			if (pluginsdisabled.indexOf(f.getName()) == -1)
				message = load(f,true);
			
			if (!message.equals(""))
				log.log(message);
		}
		
	}
	
	/**
	 * Load plugin from specific file, plugin must have <b>Plugin-Main-Class</b> attribute with class name 
	 * in <b>manifest</b> to detect main class
	 * @param file Flie to plugin
	 * @param enable Enable plugin on load
	 * @return Error message
	 */
	public static String load(File file,boolean enable)
	{
		log.log("Loading plugin " + file.getName());
		
		try 
		{
			URLClassLoader child = new URLClassLoader(
			        new URL[] {file.toURI().toURL()}
			);
			
			//getting class name
			Manifest manifest = new Manifest(new URL("jar:" + file.toURI().toURL() + "!/" + JarFile.MANIFEST_NAME).openStream());
			Attributes attribs = manifest.getMainAttributes();
			
			String classpath = attribs.getValue("Plugin-Main-Class");
			
			Class<?> classToLoad = Class.forName(classpath, true, child);

			ServerdPlugin instance = (ServerdPlugin) classToLoad.getDeclaredConstructor().newInstance();
			Plugin plugin = new Plugin(file,instance);
			
			if (enable) plugin.start();
			
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
			e.printStackTrace();
			return file.getName() + ": Plugin load failed: " + e.getMessage();
		}
		
		return "";
	}
	
	/**
	 * Adding plugin to manager
	 * @param plugin Plugin instance
	 */
	public static void addPlugin(Plugin plugin)
	{
		plugins.add(plugin);
	}
	
	/**
	 * Unloading plugin from manager
	 * @param plugin Plugin instance
	 */
	public static void unloadPlugin(Plugin plugin)
	{
		plugin.stop();
		plugins.remove(plugin);
	}
	
	/**
	 * List all plugins names
	 * @return Array of names
	 */
	public static String[] listPluginsName()
	{
		String[] s = new String[plugins.size()];
		
		for (int i = 0;i < s.length;i++)
		{
			s[i] = plugins.get(i).file.getName();
		}
		
		return s;
	}
	
	/**
	 * Returning plugin instance by name
	 * @param name Plugin name
	 * @return Plugin instance
	 */
	public static Plugin getByFileName(String name)
	{
		for (Plugin p : plugins)
			if (p.file.getName().equals(name)) return p;
		return null;
	}
	
	public static int enablePlugin(Plugin p)
	{
		pluginsdisabled.remove(p.file.getName());
		rewritePluginDisableFile();
		return p.start();
	}
	
	public static void disablePlugin(Plugin p)
	{
		pluginsdisabled.add(p.file.getName());
		rewritePluginDisableFile();
		p.stop();
	}
	
	private static void rewritePluginDisableFile()
	{
		try 
		{
			FileWriter writer = new FileWriter(pluginsdisabled_file); 
			for(String str : pluginsdisabled) 
			{
				writer.write(str + System.lineSeparator());
			}
			writer.close();
		} catch (IOException e) {
			log.log("Error writing file");
		}
	}
}
