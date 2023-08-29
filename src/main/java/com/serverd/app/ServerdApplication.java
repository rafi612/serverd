package com.serverd.app;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import com.serverd.main.Main;

/**
 * Allows to running ServerD inside plugin to create self contained app
 */
public class ServerdApplication {
	
	/**
	 * Running ServerD and loading class as plugin to create self contained app
	 * @param appClass App class object
	 */
	public static void run(Class<?> appClass) {
		run(appClass,new String[] {});
	}
	
	/**
	 * Running ServerD and loading class as plugin to create self contained app
	 * @param appClass App class object
	 * @param args Command line arguments
	 */
	public static void run(Class<?> appClass,String[] args) {
		run(appClass,args,false);
	}
	
	/**
	 * Running ServerD and loading class as plugin to create self contained app
	 * @param appClass App class object
	 * @param args Command line arguments
	 * @param otherPlugins Want to load another plugins?
	 */
	public static void run(Class<?> appClass,String[] args,boolean otherPlugins) {
		run(appClass,args,otherPlugins,getWorkDir("serverd"));
	}

	/**
	 * Running ServerD and loading class as plugin to create self contained app
	 * @param appClass App class object
	 * @param args Command line arguments
	 * @param otherPlugins Want to load another plugins?
	 * @param workdir Working directory path
	 */
	public static void run(Class<?> appClass,String[] args,boolean otherPlugins,String workdir) {
		ArrayList<String> mainArgs = new ArrayList<>();
		
		//plugin class name
		mainArgs.add("--app-class");
		mainArgs.add(appClass.getName());
		
		//other plugins
		if (!otherPlugins)
			mainArgs.add("--noplugins");
		
		//working dir
		mainArgs.add("--working-loc");
		mainArgs.add(workdir);
		
		//optional args
		mainArgs.addAll(Arrays.asList(args));
		
		//running main method with args
		Main.main(mainArgs.toArray(String[]::new));
	}
	
	/**
	 * Returns default working directory
	 * @return Working directory path.
	 */
	public static String getWorkDir(String name) {
		String osname = System.getProperty("os.name").toLowerCase();
		String userhome = System.getProperty("user.home");
		
		if (osname.startsWith("windows"))
			return Paths.get(System.getenv("APPDATA"),name).toString();
		else if (osname.contains("nux") || osname.contains("freebsd"))
			return Paths.get(userhome,".config",name).toString();
		else if (osname.contains("mac") || osname.contains("darwin"))
			return Paths.get(userhome,"Library","Application Support",name).toString();
		return userhome;
	}
}
