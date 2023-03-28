package com.serverd.command;

import java.util.ArrayList;

/**
 * Class storing all commands
 */
public class Commands 
{
	protected static ArrayList<Command> commands = new ArrayList<>();

	/**
	 * Initialize all build in commands
	 */
	public static void init()
	{
		commands.add(new Disconnect());
		commands.add(new Id());
		commands.add(new Status());
		commands.add(new To());
		commands.add(new Join());
		commands.add(new Close());
		commands.add(new Unjoin());
		commands.add(new Rawdata());
		commands.add(new Setname());
		commands.add(new PluginCommand());
		commands.add(new PluginsList());
		commands.add(new Help());
	}
	
	/**
	 * Get all build in commands
	 * @return Command {@link ArrayList}
	 */
	public static ArrayList<Command> getCommandsList()
	{
		return commands;
	}
	
	/**
	 * Getting build in commands by name 
	 * @param name Command name
	 * @return Command object
	 */
	public static Command getByName(String name)
	{
		for (Command command : commands)
			if (command.command.equals(name))
				return command;
		return null;
	}
}
