package com.serverd.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.serverd.app.ServerdApplication;
import com.serverd.log.Log;

/**
 * Plugin manager.
 */
public class PluginManager {
	public File pluginDir;
	public File pluginDataDir;
	public File pluginAppDataDir;
	public File pluginDisabledFile;
	
	public List<String> pluginsDisabled;

	public ArrayList<Plugin> plugins = new ArrayList<>();

	private final Log log = new Log("Plugin Manager");

	private final ServerdApplication app;

	public PluginManager(ServerdApplication app) {
		this.app = app;
	}
	
	/**
	 * Init method.
	 * @param workdir Working dir file
	 */
	public void init(File workdir) throws IOException {
		pluginDir = new File(workdir,"plugins");
		pluginDataDir = new File(workdir,"pluginsdata");
			
		pluginDisabledFile = new File(workdir,"plugins_disabled.conf");
		
		if (!workdir.getName().equals("serverd"))
			pluginAppDataDir = new File(workdir,"appdata");
		else
			pluginAppDataDir = null;
		
		//create plugin dir
		if (!pluginDir.exists() && !pluginDir.mkdir())
			throw new IOException("Failed to create plugin dir");
		
		if (!pluginDataDir.exists() && !pluginDataDir.mkdir())
			throw new IOException("Failed to create plugin data dir");
		
		if (!pluginDisabledFile.exists() && !pluginDisabledFile.createNewFile())
			throw new IOException("Failed to create plugin disabled file");

		pluginsDisabled = Files.readAllLines(pluginDisabledFile.toPath(), Charset.defaultCharset());
	}

	/**
	 * Loading all plugins.
	 */
	public void loadPlugins() {
		File[] files = pluginDir.listFiles();
		
		if (files != null) {
			for (File file : files) {
				try {
					load(file, !pluginsDisabled.contains(file.getName()));
				} catch (PluginLoadException e) {
					log.error(e.getPluginName() + ": " + e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Load plugin from specific file, plugin must have <b>Plugin-Main-Class</b> attribute with class name .
	 * in <b>manifest</b> to detect main class
	 * @param file File to plugin
	 * @param enable Enable plugin on load
	 * @throws PluginLoadException when plugin was not successfully loaded.
	 */
	public void load(File file,boolean enable) throws PluginLoadException {
		log.info("Loading plugin " + file.getName());

		try {
			URLClassLoader classloader = new URLClassLoader(
			        new URL[] { file.toURI().toURL() }
			);
			
			//getting class name
			Manifest manifest = new Manifest(new URL("jar:" + file.toURI().toURL() + "!/" + JarFile.MANIFEST_NAME).openStream());
			Attributes attribs = manifest.getMainAttributes();
			
			if (attribs.getValue("Plugin-Main-Class") == null) {
				classloader.close();
				throw new PluginLoadException(file.getName(),"Broken plugin, Plugin-Main-Class manifest attribute not found");
			}
			
			String classname = attribs.getValue("Plugin-Main-Class");
			
			Class<?> classToLoad = Class.forName(classname, true, classloader);

			ServerdPlugin instance = (ServerdPlugin) classToLoad.getDeclaredConstructor().newInstance();
			Plugin plugin = new Plugin(file.getName(),this,instance);
			
			if (enable) 
				plugin.start();
			
			plugins.add(plugin);
			
		} catch (ClassNotFoundException e) {
			throw new PluginLoadException(file.getName(),"Plugin Main class not found",e);
		} catch (NoClassDefFoundError e) {
			throw new PluginLoadException(file.getName(),"Plugin load failed, can't load class: " + e.getMessage(),e);
		} catch (Exception e) {
			throw new PluginLoadException(file.getName(),"Plugin load failed: " + e.getMessage(),e);
		}		
	}

	public void shutdown() {
		log.info("Stopping plugins...");
		for (Plugin plugin : plugins)
			plugin.stop();
	}
	
	/**
	 * Adding plugin to manager.
	 * @param plugin Plugin instance
	 */
	public void addPlugin(Plugin plugin) {
		plugins.add(plugin);
	}
	
	/**
	 * Unloading plugin from manager.
	 * @param plugin Plugin instance
	 */
	public void unloadPlugin(Plugin plugin) {
		plugin.stop();
		plugins.remove(plugin);
	}
	
	/**
	 * Unloading all plugins.
	 */
	public void unloadAllPlugins() {
		for (int i = 0;i < plugins.size();i++)
			unloadPlugin(plugins.get(i));
	}
	
	/**
	 * List all plugins names.
	 * @return Array of names
	 */
	public String[] listPluginsName() {
		String[] pluginsNames = new String[plugins.size()];
		
		for (int i = 0;i < pluginsNames.length;i++)
			pluginsNames[i] = plugins.get(i).name;
		
		return pluginsNames;
	}
	
	/**
	 * Returning plugin instance by name.
	 * @param name Plugin name
	 * @return Plugin instance
	 */
	public Plugin getByFileName(String name) {
		for (Plugin plugin : plugins)
			if (plugin.name.equals(name))
				return plugin;
		return null;
	}
	
	/**
	 * Getting plugin instance by ID.
	 * @param id Plugin ID
	 * @return Plugin instance by ID
	 */
	public Plugin getPluginByID(int id) {
		if (id < 0 || id > plugins.size())
			return null;
		return plugins.get(id);
	}
	
	
	/**
	 * Getting plugin ID by plugin instance.
	 * @param plugin Plugin instance
	 * @return plugin ID
	 */
	public int getIDByPlugin(Plugin plugin) {
		return plugins.indexOf(plugin);
	}
	
	/**
	 * Get plugins loaded amount.
	 * @return plugin loaded amount
	 */
	public int getPluginsAmountLoaded() {
		return plugins.size();
	}
	
	/**
	 * Enabling plugin.
	 * @param plugin Plugin instance
	 * @return true if plugin load successfully
	 */
	public boolean enablePlugin(Plugin plugin) {
		pluginsDisabled.remove(plugin.name);
		rewritePluginDisableFile();
		return plugin.start();
	}
	
	/**
	 * Disabling plugin.
	 * @param plugin Plugin instance
	 */
	public void disablePlugin(Plugin plugin) {
		pluginsDisabled.add(plugin.name);
		rewritePluginDisableFile();
		plugin.stop();
	}
	
	/**
	 * Returns all plugins.
	 * @return all plugins table
	 */
	public Plugin[] getPlugins() {
		return plugins.toArray(Plugin[]::new);
	}


	public ServerdApplication getApp() {
		return app;
	}
	
	/**
	 * Rewriting plugins_disabled.conf.
	 */
	private void rewritePluginDisableFile() {
		try (FileWriter writer = new FileWriter(pluginDisabledFile)) {
			for (String str : pluginsDisabled) 
				writer.write(str + System.lineSeparator());
		} catch (IOException e) {
			log.error("Error writing file plugins_disabled.conf");
		}
	}
}
