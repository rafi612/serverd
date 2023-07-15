package com.serverd.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger class
 */
public class Log {
	/** Colors*/
	public static final String ANSI_RESET = "\u001B[0m",
	ANSI_BLACK = "\u001B[30m",ANSI_RED = "\u001B[31m",
	ANSI_GREEN = "\u001B[32m",ANSI_YELLOW = "\u001B[33m",
	ANSI_BLUE = "\u001B[34m",ANSI_PURPLE = "\u001B[35m",
	ANSI_CYAN = "\u001B[36m",ANSI_WHITE = "\u001B[37m";
	
	private String name;
	
	/**
	 * Default constructor
	 * @param name Logger name
	 */
	public Log(String name) {
		this.name = name;
	}

	/**
	 * Returning name of logger
	 * @return Logger name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setting logger name
	 * @param name Name of logger
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Logging as Info level
	 * @param message Message to log
	 */
	public synchronized void info(String message) {
		String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
		System.out.println(ANSI_WHITE + date + ANSI_RESET + " [" + name  + "] " + ANSI_GREEN + 
				"INFO " + ANSI_RESET + message);
	}
	
	/**
	 * Logging as Warn level
	 * @param message Message to log
	 */
	public synchronized void warn(String message) {
		String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
		System.out.println(ANSI_WHITE + date + ANSI_RESET + " [" + name  + "] " + ANSI_YELLOW + 
				"WARN " + ANSI_RESET + message);
	}
	
	/**
	 * Logging as Error level
	 * @param message Message to log
	 */
	public synchronized void error(String message) {
		String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
		System.out.println(ANSI_WHITE + date + ANSI_RESET + " [" + name  + "] " + ANSI_RED + 
				"ERROR " + ANSI_RESET + message);
	}
	
	/**
	 * Logging as Debug level
	 * @param message Message to log
	 */
	public synchronized void debug(String message) {
		String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
		System.out.println(ANSI_WHITE + date + ANSI_RESET + " [" + name  + "] " + ANSI_PURPLE + 
				"DEBUG " + ANSI_RESET + message);
	}
	
	/**
	 * Logging as Trace level
	 * @param message Message to log
	 */
	public synchronized void trace(String message) {
		String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
		System.out.println(ANSI_WHITE + date + ANSI_RESET + " [" + name  + "] " + ANSI_BLUE + 
				"TRACE " + ANSI_RESET + message);
	}
}
