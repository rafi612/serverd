package com.serverd.plugin;

import java.io.File;

/**
 * Utilities for loading plugins.
 */
public class PluginUtils {
	
	/**
	 * Loading plugin from classpath by plugin name.
	 * @param className Main plugin class name.
	 * @param pluginManager Plugin manager.
	 * @return Plugin instance.
	 * @throws PluginLoadException when plugin load failed.
	 */
	public static Plugin loadPluginFromClassName(String className,PluginManager pluginManager) throws PluginLoadException {
		Plugin plugin = loadPluginInstanceFromClassName(className,pluginManager);
		
		plugin.start();
		pluginManager.addPlugin(plugin);
		
		return plugin;
	}
	
	/**
	 * Loading plugin object without starting it from classpath by plugin name.
	 * @param className Main plugin class name.
	 * @param pluginManager Plugin manager.
	 * @return Plugin instance.
	 * @throws PluginLoadException when plugin load failed.
	 */
	public static Plugin loadPluginInstanceFromClassName(String className, PluginManager pluginManager) throws PluginLoadException  {
		try {
			String classFile = className.replace(".", File.separator) + ".class";
			
			Class<?> classToLoad = PluginUtils.class.getClassLoader().loadClass(className);
			
			ServerdPlugin instance = (ServerdPlugin) classToLoad.getDeclaredConstructor().newInstance();

            return new Plugin(classFile,pluginManager,instance);
		} catch (ClassNotFoundException e) {
			throw new PluginLoadException(className,"Plugin Main class not found",e);
		} catch (NoClassDefFoundError e) {
			throw new PluginLoadException(className,"Plugin load failed, can't load class: " + e.getMessage(),e);
		} catch (Exception e) {
			throw new PluginLoadException(className,"Plugin load failed: " + e.getMessage(),e);
		}	
	}
	
	/**
	 * Loading plugin as app from class name.
	 * @param appClass Main app class name.
	 * @throws PluginLoadException when plugin load failed.
	 */
	public static void loadPluginAsApp(String appClass,PluginManager pluginManager) throws PluginLoadException {
		Plugin plugin = PluginUtils.loadPluginInstanceFromClassName(appClass,pluginManager);
		plugin.markAsApp();
		pluginManager.addPlugin(plugin);
		plugin.start();
	}
	
}
