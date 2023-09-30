package com.serverd.utils;

/**
 * Utilities class.
 */
public class Utils {
	/**
	 * Sleep for specific time, can be used instead {@link Thread#sleep(long)} to not have to catch {@link InterruptedException}.
	 * @param millis Time in milliseconds
	 */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
