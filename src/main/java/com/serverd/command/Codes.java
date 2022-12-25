package com.serverd.command;

/**
 * Standardized codes for communication with client
 * OK - operation executed successfully
 * OK (message) - operation executed successfully with message
 * ERROR (message) - operation failed with message
 */
public interface Codes 
{
	/**
	 * Returning standardized successful code
	 * use in {@link Command#execute} in inherited class to send success to connected client
	 * @return successful code
	 */
	public default String ok()
	{
		return "OK";
	}
	
	/**
	 * Returning standardized successful code with message
	 * use in {@link Command#execute} in inherited class to send success to connected client
	 * @return successful code
	 */
	public default String ok(String message)
	{
		return "OK " + message;
	}
	
	/**
	 * Returning standardized error code with message
	 * use in {@link Command#execute} in inherited class to send success to connected client
	 * @return error code with message
	 */
	public default String error(String message)
	{
		return "ERROR " + message;
	}
	
	/**
	 * Returning unknown command code
	 * Used in server internal code
	 * @return unknown command code
	 */
	public static String unknownCommand()
	{
		return "UNKNOWN_COMMAND";
	}
}
