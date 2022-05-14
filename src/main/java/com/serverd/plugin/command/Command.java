package com.serverd.plugin.command;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

/**
 * Abstract class to creating custom commands
 */
public abstract class Command
{
	public String command = "",help = "";
	
	public Command()
	{
		
	}
	
	/**
	 * Checking amount of arguments
	 * @param args Arguments
	 * @param length Arguments length
	 * @return Good of arguments
	 */
	protected int checkArgs(String[] args,int length)
	{
		
		if (args.length < length) 
		{
			return -1;
		}
		else if (args.length > length)
		{
			return 1;
		}
		else return 0;
	}
	
	/**
	 * Executing when command is called
	 * @param args Command arguments
	 * @param client Current client instance
	 * @param plugin Plugin instance
	 */
	public abstract void execute(String[] args,Client client,Plugin plugin);
	

}
