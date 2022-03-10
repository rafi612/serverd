package com.serverd.log;

public class Log 
{
	
	public static void log(String thread,String message)
	{
		System.out.println("["  + thread  + "] " + message);
	}

}
