package com.serverd.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.serverd.main.Main;

/**
 * Allows to running ServerD inside plugin for debugging
 */
public class Debug 
{
	/**
	 * Loading plugin from classpath by plugin name
	 * @param classname Main plugin class name
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static void loadPluginFromClassName(String classname) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		File classfile = new File(classname.replace(".", File.separator) + ".class");
		
		Class<?> classToLoad = Debug.class.getClassLoader().loadClass(classname);
		
		ServerdPlugin instance = (ServerdPlugin) classToLoad.getDeclaredConstructor().newInstance();
		Plugin plugin = new Plugin(classfile,instance);
		
		plugin.start();
		
		PluginManager.addPlugin(plugin);
	}
	
	/**
	 * 
	 * @param classname Plugin main classname
	 * @param plugins Want to load another plugins?
	 * @param args ServerD arguments
	 * @return ServerD/Java exitcode
	 */
	public static int testPlugin(String classname,boolean plugins,String[] args)
	{
		ProcessBuilder builder = new ProcessBuilder();
		//command
		builder.command("java","-cp",System.getProperty("java.class.path"),Main.class.getName(),plugins ? "" : "--noplugins","--plugin-debug",classname,String.join(" ", args));
		builder.directory(new File(System.getProperty("user.dir")));
		
		//redirect output to terminal
		builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		builder.redirectError(ProcessBuilder.Redirect.INHERIT);
		builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
		
		//run
		Process p;
		try 
		{
			p = builder.start();
			return p.waitFor();
		} 
		catch (IOException | InterruptedException e) 
		{
			e.printStackTrace();
		}
		return -1;
	}

}
