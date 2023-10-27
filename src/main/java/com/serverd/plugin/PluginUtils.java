package com.serverd.plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Plugin utilities.
 */
public class PluginUtils {
	
	/**
	 * Loading plugin from classpath by plugin name.
	 * @param classname Main plugin class name
	 * @return Plugin instance.
	 * @throws PluginLoadException when plugin load failed.
	 */
	public static Plugin loadPluginFromClassName(String classname) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, PluginLoadException {
		Plugin plugin = loadPluginInstanceFromClassName(classname);
		
		plugin.start();
		PluginManager.addPlugin(plugin);
		
		return plugin;
	}
	
	/**
	 * Loading plugin object without starting it from classpath by plugin name.
	 * @param classname Main plugin class name.
	 * @return Plugin instance.
	 * @throws PluginLoadException when plugin load failed.
	 */
	public static Plugin loadPluginInstanceFromClassName(String classname) throws PluginLoadException  {
		try {
			String classfile = classname.replace(".", File.separator) + ".class";
			
			Class<?> classToLoad = PluginUtils.class.getClassLoader().loadClass(classname);
			
			ServerdPlugin instance = (ServerdPlugin) classToLoad.getDeclaredConstructor().newInstance();

            return new Plugin(classfile,instance);
		} catch (ClassNotFoundException e) {
			throw new PluginLoadException(classname,"Plugin Main class not found",e);
		} catch (NoClassDefFoundError e) {
			throw new PluginLoadException(classname,"Plugin load failed, can't load class: " + e.getMessage(),e);
		} catch (Exception e) {
			throw new PluginLoadException(classname,"Plugin load failed: " + e.getMessage(),e);
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
