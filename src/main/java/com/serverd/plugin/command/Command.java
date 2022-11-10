package com.serverd.plugin.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

/**
 * Abstract class to creating custom commands
 */
public abstract class Command
{
	/** Command name*/
	public String command = "";
	/** Command help*/
	public String help = "";
	/** Default constructor*/
	public Command() {}
	
	/**
	 * Checking amount of arguments and sending message to client
	 * @param args Arguments
     * @param client Client instance
	 * @param length Arguments length
	 * @return <b>0</b> when arguments are good,
	 * <b>-1</b> when there are too few arguments,
	 * <b>1</b> when there are too many arguments
	 * @throws IOException when client throw {@link IOException}
	 * @see Command#checkArgs checkArgs
	 */
	protected int checkArgs(String[] args,Client client,int length) throws IOException
	{
		int code = checkArgs(args, length);
		if (code == -1)
		{
			client.send("Missing argument");
			return -1;
		}
		else if (code == 1)
		{
			client.send("Too much arguments");
			return 1;
		}
		else return code;
	}
	
	
	/**
	 * Checking amount of arguments
	 * @param args Arguments
	 * @param length Arguments length
	 * @return <b>0</b> when arguments are good,
	 * <b>-1</b> when there are too few arguments,
	 * <b>1</b> when there are too many arguments
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
	 * @throws IOException when client throw {@link IOException}
	 */
	public abstract void execute(String[] args,Client client,Plugin plugin) throws IOException;
	
}