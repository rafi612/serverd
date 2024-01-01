package com.serverd.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger class.
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
	 * Default constructor.
	 * @param name Logger name.
	 */
	public Log(String name) {
		this.name = name;
	}

	/**
	 * @return Logger name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setting logger name.
	 * @param name Name of logger.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Logging as Info level.
	 * @param message Message to log.
	 */
	public void info(String message) {
		log("INFO",ANSI_GREEN,message);
	}
	
	/**
	 * Logging as Warning level.
	 * @param message Message to log.
	 */
	public void warn(String message) {
		log("WARN",ANSI_YELLOW,message);
	}
	
	/**
	 * Logging as Error level.
	 * @param message Message to log.
	 */
	public void error(String message) {
		log("ERROR",ANSI_RED,message);
	}
	
	/**
	 * Logging as Debug level.
	 * @param message Message to log.
	 */
	public void debug(String message) {
		log("DEBUG",ANSI_PURPLE,message);
	}
	
	/**
	 * Logging as Trace level.
	 * @param message Message to log.
	 */
	public void trace(String message) {
		log("TRACE",ANSI_BLUE,message);
	}

	/**
	 * Logging as described level.
	 * @param level Log level.
	 * @param color Color ANSI code.
	 * @param message Message.
	 */
	protected synchronized void log(String level,String color,String message) {
		String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
		System.out.println(ANSI_WHITE + date + ANSI_RESET + " [" + formatName(name)  + "] "
				+ "-- "
				+ color + level + " " + ANSI_RESET + message);
	}

	private String formatName(String name) {
		StringBuilder sb = new StringBuilder();

		String packageName = shortenPackageName(name);
		sb.append(String.format("%-30s", packageName));
		sb.append(" ");

		return sb.toString();
	}

	private String shortenPackageName(String fullPackageName) {
		String[] packages = fullPackageName.split("\\.");
		StringBuilder sb = new StringBuilder();

		if (packages.length > 1) {
			for (int i = 0; i < packages.length - 2; i++) {
				sb.append(packages[i].charAt(0)).append(".");
			}
			sb.append(packages[packages.length - 2]);
			sb.append(".");
		}

		sb.append(packages[packages.length - 1]);

		return sb.toString();
	}

	/**
	 * Return a logger named corresponding to the class passed as parameter.
	 * @param clazz The returned logger will be named after clazz.
	 * @return Logger instance.
	 */
	public static Log get(Class<?> clazz) {
		return new Log(clazz.getName());
	}
}
