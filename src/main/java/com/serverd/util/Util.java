package com.serverd.util;

/**
 * Utilities class.
 */
public class Util {
	/**
	 * Sleep for specific time, can be used instead {@link Thread#sleep(long)} to not have to catch {@link InterruptedException}.
	 * @param milis Time in miliseconds
	 */
	public static void sleep(long milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
