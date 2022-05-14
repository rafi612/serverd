package com.serverd.log;

public class Log 
{
	public String name;
	public Log(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Log to stdin
	 * @param message Message
	 */
	public void log(String message)
	{
		System.out.println("["  + name  + "] " + message);
	}

}
