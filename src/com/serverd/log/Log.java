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

	public void log(String message)
	{
		System.out.println("["  + name  + "] " + message);
	}
	
//	public static void log(String thread,String message)
//	{
//		System.out.println("["  + thread  + "] " + message);
//	}

}
