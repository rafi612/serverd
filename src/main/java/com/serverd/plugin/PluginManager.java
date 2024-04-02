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

import com.serverd.app.DirectorySchema;
import com.serverd.app.ServerdApplication;
import com.serverd.log.Log;

/**
 * Class used to manage loaded plugins.
 */
public class PluginManager {
	protected File pluginDir;
	protected File pluginDataDir;
	protected File pluginAppDataDir;
	protected File pluginDisabledFile;
	
	public List<String> pluginsDisabled;

	public ArrayList<Plugin> plugins = new ArrayList<>();

	private final Log log = Log.get(PluginManager.class);

	private final ServerdApplication app;

	public PluginManager(ServerdApplication app) {
		this.app = app;
	}
	
	/**
	 * Initializing plugin manager.
	 * @param workdir Working dir file.
	 */
	public void init(File workdir, DirectorySchema directorySchema) throws IOException {
		pluginDir = directorySchema.get(workdir, DirectorySchema.PLUGIN_DIR);
		pluginDataDir = directorySchema.get(workdir, DirectorySchema.PLUGINS_DATA_DIR);
			
		pluginDisabledFile = new File(directorySchema.get(workdir, DirectorySchema.SERVERD_ROOT_DIR), "plugins_disabled.conf");
		
		if (!workdir.getName().equals("serverd"))
			pluginAppDataDir = directorySchema.get(workdir, DirectorySchema.APP_DATA_DIR);
		else
			pluginAppDataDir = null;

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
	 * Load plugin from specific file.
	 * Plugin must have <b>Plugin-Main-Class</b> attribute with class name in <b>manifest</b> to detect main class.
	 * @param file File to plugin.
	 * @param enable Enable plugin on load.
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
			Attributes attributes = manifest.getMainAttributes();
			
			if (attributes.getValue("Plugin-Main-Class") == null) {
				classloader.close();
				throw new PluginLoadException(file.getName(),"Broken plugin, Plugin-Main-Class manifest attribute not found");
			}
			
			String className = attributes.getValue("Plugin-Main-Class");
			
			Class<?> classToLoad = Class.forName(className, true, classloader);

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
	 * @param plugin Plugin instance.
	 */
	public void addPlugin(Plugin plugin) {
		plugins.add(plugin);
	}
	
	/**
	 * Unloading plugin from manager.
	 * @param plugin Plugin instance.
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
	 * Returns array of all plugins names.
	 */
	public String[] listPluginsName() {
		String[] pluginsNames = new String[plugins.size()];
		
		for (int i = 0;i < pluginsNames.length;i++)
			pluginsNames[i] = plugins.get(i).getName();
		
		return pluginsNames;
	}
	
	/**
	 * Returns plugin instance by name.
	 * @param name Plugin name.
	 */
	public Plugin getByFileName(String name) {
		for (Plugin plugin : plugins)
			if (plugin.getName().equals(name))
				return plugin;
		return null;
	}
	
	/**
	 * Returns plugin instance by ID.
	 * @param id Plugin ID
	 */
	public Plugin getPluginByID(int id) {
		if (id < 0 || id > plugins.size())
			return null;
		return plugins.get(id);
	}
	
	
	/**
	 * Returns plugin ID by plugin instance.
	 * @param plugin Plugin instance
	 */
	public int getIDByPlugin(Plugin plugin) {
		return plugins.indexOf(plugin);
	}
	
	/**
	 * Returns plugins loaded amount.
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
		pluginsDisabled.remove(plugin.getName());
		rewritePluginDisableFile();
		return plugin.start();
	}
	
	/**
	 * Disabling plugin.
	 * @param plugin Plugin instance
	 */
	public void disablePlugin(Plugin plugin) {
		pluginsDisabled.add(plugin.getName());
		rewritePluginDisableFile();
		plugin.stop();
	}
	
	/**
	 * Returns all plugins array.
	 */
	public Plugin[] getPlugins() {
		return plugins.toArray(Plugin[]::new);
	}

	/**
	 * Returns application context object.
	 */
	public ServerdApplication getApp() {
		return app;
	}

	private void rewritePluginDisableFile() {
		try (FileWriter writer = new FileWriter(pluginDisabledFile)) {
			for (String str : pluginsDisabled) 
				writer.write(str + System.lineSeparator());
		} catch (IOException e) {
			log.error("Error writing file plugins_disabled.conf");
		}
	}
}
