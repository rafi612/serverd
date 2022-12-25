package com.serverd.command;

import java.util.ArrayList;

public class Commands 
{
	public static ArrayList<Command> commands = new ArrayList<>();
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
	
	public static ArrayList<Command> getCommandsList()
	{
		return commands;
	}
	
	public static Command getByName(String name)
	{
		for (Command command : commands)
			if (command.command.equals(name))
				return command;
		return null;
	}
}
