package com.serverd.plugin;

import java.io.File;

/**
 * Plugin utilities.
 */
public class PluginUtils {
	
	/**
	 * Loading plugin from classpath by plugin name.
	 * @param className Main plugin class name
	 * @return Plugin instance.
	 * @throws PluginLoadException when plugin load failed.
	 */
	public static Plugin loadPluginFromClassName(String className) throws PluginLoadException {
		Plugin plugin = loadPluginInstanceFromClassName(className);
		
		plugin.start();
		PluginManager.addPlugin(plugin);
		
		return plugin;
	}
	
	/**
	 * Loading plugin object without starting it from classpath by plugin name.
	 * @param className Main plugin class name.
	 * @return Plugin instance.
	 * @throws PluginLoadException when plugin load failed.
	 */
	public static Plugin loadPluginInstanceFromClassName(String className) throws PluginLoadException  {
		try {
			String classFile = className.replace(".", File.separator) + ".class";
			
			Class<?> classToLoad = PluginUtils.class.getClassLoader().loadClass(className);
			
			ServerdPlugin instance = (ServerdPlugin) classToLoad.getDeclaredConstructor().newInstance();

            return new Plugin(classFile,instance);
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
	public static void loadPluginAsApp(String appClass) throws PluginLoadException {
		Plugin plugin = PluginUtils.loadPluginInstanceFromClassName(appClass);
		plugin.markAsApp();
		PluginManager.addPlugin(plugin);
		plugin.start();
	}
	
}
