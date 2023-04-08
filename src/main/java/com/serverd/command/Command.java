package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

/**
 * Abstract class to creating custom commands
 */
public abstract class Command implements Codes,Cloneable
{
	/** Command name*/
	public String command = "";
	/** Command help*/
	public String help = "";
	/** Default constructor*/
	public Command() {}
	
	/** Less arguments */
	protected static final int ARGS_LESS = 0b00000001;
	/** Good arguments */
	protected static final int ARGS_GOOD = 0b00000010; 
	/** More arguments */
	protected static final int ARGS_MORE = 0b00000100; 
	
	public boolean stayAlive = false,runned = false;
	
	/**
	 * Checking amount of arguments
	 * @param args Arguments
	 * @param length Arguments length
	 * @param flag One of {@link ARGS_LESS},{@link ARGS_GOOD},{@link ARGS_MORE}
	 * @return true when arguments are valid
	 */
	protected boolean checkArgs(String[] args,int length,int flag) 
	{
		int argsCount = args.length;

		if (argsCount < length)
			return (flag & ARGS_LESS) == ARGS_LESS;
		else if (argsCount > length)
			return (flag & ARGS_MORE) == ARGS_MORE;
		else
			return (flag & ARGS_GOOD) == ARGS_GOOD;
	}
	
	/**
	 * Checking amount of arguments, using {@link ARGS_GOOD} as default
	 * @param args Arguments
	 * @param length Arguments length
	 * @return true when arguments are valid
	 */
	protected boolean checkArgs(String[] args,int length)
	{
		return checkArgs(args,length,ARGS_GOOD);
	}
	
	/**
	 * Checking amount of arguments and sending message to client
	 * @param args Arguments
	 * @param client Client instance 
	 * @param length Arguments length
	 * @param flag One of {@link ARGS_LESS},{@link ARGS_GOOD},{@link ARGS_MORE}
	 * @return true when arguments are valid
	 * @throws IOException when client throw {@link IOException}
	 */
	protected boolean checkArgs(String[] args,Client client,int length,int flag) throws IOException 
	{
		int argsCount = args.length;

		if (argsCount < length) 
		{
			if ((flag & ARGS_LESS) == ARGS_LESS)
				return true;
			else 
				client.send(error("Too few arguments"));
			return false;
		} 
		else if (argsCount > length)
		{
			if ((flag & ARGS_MORE) == ARGS_MORE) 
				return true;
			else 
				client.send(error("Too many arguments"));
			return false;
		} 
		else
		{
			if ((flag & ARGS_GOOD) == ARGS_GOOD)
				return true;
			else 
				client.send(error("Bad number of arguments"));
			return false;
		}
	}
	
	/**
	 * Checking amount of arguments and sending message to client, using {@link ARGS_GOOD} as default
	 * @param args Arguments
	 * @param client Client instance 
	 * @param length Arguments length
	 * @return true when arguments are valid
	 * @throws IOException when client throw {@link IOException}
	 */
	protected boolean checkArgs(String[] args,Client client,int length) throws IOException 
	{
		return checkArgs(args,client,length,ARGS_GOOD);
	}
	
	public void processReceive(byte[] bytes,Client client) throws IOException {}
	
	public String getName()
	{
		return command;
	}
	
	public void stayAlive()
	{
		stayAlive = true;
	}
	
	public boolean isStayAlive()
	{
		return stayAlive;
	}
	
	public void done()
	{
		runned = false;
		stayAlive = false;
	}
	
	public boolean isRunned()
	{
		return runned;
	}
	
	
	/**
	 * Executing when command is called
	 * @param args Command arguments
	 * @param client Current client instance
	 * @param plugin Plugin instance
	 * @throws IOException when client throw {@link IOException}
	 */
	public abstract void execute(String[] args,Client client,Plugin plugin) throws IOException;
	
    @Override
    public Object clone() throws CloneNotSupportedException 
    {
        return super.clone();
    }
}