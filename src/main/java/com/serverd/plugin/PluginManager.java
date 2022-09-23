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
	public static String pluginDir = Paths.get(Main.workingdir,"plugins").toString();
	public static String pluginDataDir = Paths.get(Main.workingdir,"pluginsdata").toString();
	
	public static File pluginDisabledFile = Paths.get(Main.workingdir,"plugins_disabled.conf").toFile();
	
	public static List<String> pluginsDisabled;
	
	public static ArrayList<Plugin> plugins = new ArrayList<Plugin>();

	private static Log log = new Log("Plugin Manager");

	/**
	 * Loading all plugins
	 * @throws IOException 
	 */
	public static void loadPlugins() throws IOException
	{		
		//create plugin dir
		File pdir = new File(pluginDir);
		if (!pdir.exists())
			pdir.mkdirs();
		
		File pdatadir = new File(pluginDataDir);
		if (!pdatadir.exists())
			pdatadir.mkdirs();
		
		if (!pluginDisabledFile.exists())
			pluginDisabledFile.createNewFile();
		
		pluginsDisabled = Files.readAllLines(pluginDisabledFile.toPath(), Charset.defaultCharset());
		
		File[] files = pdir.listFiles();
		
		for (File file : files)
		{
			String message = "";
			if (pluginsDisabled.indexOf(file.getName()) == -1)
				message = load(file,true);
			
			if (!message.equals(""))
				log.error(message);
		}
	}
	
	/**
	 * Load plugin from specific file, plugin must have <b>Plugin-Main-Class</b> attribute with class name 
	 * in <b>manifest</b> to detect main class
	 * @param file Flie to plugin
	 * @param enable Enable plugin on load
	 * @return Error message ("" if success)
	 */
	public static String load(File file,boolean enable)
	{
		log.info("Loading plugin " + file.getName());
		
		try 
		{
			URLClassLoader classloader = new URLClassLoader(
			        new URL[] {file.toURI().toURL()}
			);
			
			//getting class name
			Manifest manifest = new Manifest(new URL("jar:" + file.toURI().toURL() + "!/" + JarFile.MANIFEST_NAME).openStream());
			Attributes attribs = manifest.getMainAttributes();
			
			
			if (attribs.getValue("Plugin-Main-Class") == null)
			{
				classloader.close();
				return file.getName() + ": Broken plugin, Plugin-Main-Class manifest attribute not found";
			}
			
			String classname = attribs.getValue("Plugin-Main-Class");
			
			Class<?> classToLoad = Class.forName(classname, true, classloader);

			ServerdPlugin instance = (ServerdPlugin) classToLoad.getDeclaredConstructor().newInstance();
			Plugin plugin = new Plugin(file,instance);
			
			if (enable) plugin.start();
			
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
	 * Unloading all plugins
	 */
	public static void unloadAllPlugins()
	{
		for (int i = 0;i < plugins.size();i++)
			unloadPlugin(plugins.get(i));
	}
	
	/**
	 * List all plugins names
	 * @return Array of names
	 */
	public static String[] listPluginsName()
	{
		String[] pluginsNames = new String[plugins.size()];
		
		for (int i = 0;i < pluginsNames.length;i++)
			pluginsNames[i] = plugins.get(i).file.getName();
		
		return pluginsNames;
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
	
	/**
	 * Getting plugin instance by ID
	 * @param id Plugin ID
	 * @return Plugin instance by ID
	 */
	public static Plugin getPluginByID(int id)
	{
		if (id < 0 || id > plugins.size())
			return null;
		return plugins.get(id);
	}
	
	/**
	 * Get plugins loaded amount
	 * @return plugin loaded amount
	 */
	public static int getPluginsAmountLoaded()
	{
		return plugins.size();
	}
	
	/**
	 * Enabling plugin
	 * @param plugin Plugin instance
	 * @return plugin error code
	 */
	public static int enablePlugin(Plugin plugin)
	{
		pluginsDisabled.remove(plugin.file.getName());
		rewritePluginDisableFile();
		return plugin.start();
	}
	
	/**
	 * Disabling plugin
	 * @param plugin Plugin instance
	 */
	public static void disablePlugin(Plugin plugin)
	{
		pluginsDisabled.add(plugin.file.getName());
		rewritePluginDisableFile();
		plugin.stop();
	}
	
	/**
	 * Rewriting plugins_disabled.conf
	 */
	private static void rewritePluginDisableFile()
	{
		try (FileWriter writer = new FileWriter(pluginDisabledFile)) 
		{
			for(String str : pluginsDisabled) 
			{
				writer.write(str + System.lineSeparator());
			}
			writer.close();
		} 
		catch (IOException e) 
		{
			log.error("Error writing file plugins_disabled.conf");
		}
	}
}
