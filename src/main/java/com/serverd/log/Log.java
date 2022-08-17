package com.serverd.log;

public class Log 
{
	public String name;
	public Log(String name)
	{
		this.name = name;
	}

	/**
	 * Returning name of logger
	 * @return Logger name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Setting logger name
	 * @param name Name of logger
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Log to stdin
	 * @param message Message
	 */
	public synchronized void log(String message)
	{
		System.out.println("["  + name  + "] " + message);
	}
	
	public synchronized void info(String message)
	{
		System.out.println("["  + name  + "] INFO:" + message);
	}
	
	public synchronized void warn(String message)
	{
		System.out.println("["  + name  + "] WARN:" + message);
	}
	
	public synchronized void error(String message)
	{
		System.out.println("["  + name  + "] ERROR:" + message);
	}

}
