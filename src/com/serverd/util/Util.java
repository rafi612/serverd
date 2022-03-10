package com.serverd.util;

public class Util {

	public static void sleep(long milis)
	{
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
