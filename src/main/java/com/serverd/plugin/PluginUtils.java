package com.serverd.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import com.serverd.main.Main;

/**
 * Plugin utilities
 */
public class PluginUtils {
	/**
	 * 
	 * @param classname Plugin main classname
	 * @param plugins Want to load another plugins?
	 * @param args ServerD arguments
	 * @return ServerD/Java exitcode
	 */
	public static int testPlugin(String classname,boolean plugins,String[] args) {
		ProcessBuilder builder = new ProcessBuilder();

		ArrayList<String> command = new ArrayList<>(Arrays.asList("java","-cp",System.getProperty("java.class.path"),Main.class.getName(),plugins ? "" : "--noplugins","--plugin-debug",classname));
		if (args != null)
			command.addAll(Arrays.asList(args));
		
		//command
		builder.command(command);
		builder.directory(new File(System.getProperty("user.dir")));
		
		//redirect output to terminal
		builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		builder.redirectError(ProcessBuilder.Redirect.INHERIT);
		builder.redirectInput(ProcessBuilder.Redirect.INHERIT);

		//run
		try {
			Process process = builder.start();
			return process.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Loading plugin from classpath by plugin name
	 * @param classname Main plugin class name
	 * @return ID of loaded plugin
	 */
	public static int loadPluginFromClassName(String classname) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String classfile = classname.replace(".", File.separator) + ".class";
		
		Class<?> classToLoad = PluginUtils.class.getClassLoader().loadClass(classname);
		
		ServerdPlugin instance = (ServerdPlugin) classToLoad.getDeclaredConstructor().newInstance();
		Plugin plugin = new Plugin(classfile,instance);
		
		plugin.start();
		
		PluginManager.addPlugin(plugin);
		
		return PluginManager.getPluginsAmountLoaded() - 1;
	}
	
}