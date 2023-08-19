package com.serverd.plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Plugin utilities
 */
public class PluginUtils {
	
	/**
	 * Loading plugin from classpath by plugin name
	 * @param classname Main plugin class name
	 * @return Plugin instance
	 */
	public static Plugin loadPluginFromClassName(String classname) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Plugin plugin = loadPluginInstanceFromClassName(classname);
		
		plugin.start();
		PluginManager.addPlugin(plugin);
		
		return plugin;
	}
	
	/**
	 * Loading plugin object without starting it from classpath by plugin name 
	 * @param classname Main plugin class name
	 * @return Plugin instance
	 */
	public static Plugin loadPluginInstanceFromClassName(String classname) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String classfile = classname.replace(".", File.separator) + ".class";
		
		Class<?> classToLoad = PluginUtils.class.getClassLoader().loadClass(classname);
		
		ServerdPlugin instance = (ServerdPlugin) classToLoad.getDeclaredConstructor().newInstance();
		Plugin plugin = new Plugin(classfile,instance);
		
		return plugin;
	}
	
	/**
	 * Loading plugin as app from class name
	 * @param appClass Main app class name
	 */
	public static void loadPluginAsApp(String appClass) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Plugin plugin = PluginUtils.loadPluginFromClassName(appClass);
		plugin.markAsApp();
		PluginManager.addPlugin(plugin);
		plugin.start();
	}
	
}