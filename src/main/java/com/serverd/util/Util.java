package com.serverd.util;

/**
 * Utilities class
 */
public class Util 
{
	/**
	 * Sleep for specific time, can be used instead Thread.sleep() to not have to catch InterruptedException
	 * @param milis Time in miliseconds
	 */
	public static void sleep(long milis)
	{
		try
        {
			Thread.sleep(milis);
		} 
        catch (InterruptedException e) 
        {
			e.printStackTrace();
		}
	}

}
