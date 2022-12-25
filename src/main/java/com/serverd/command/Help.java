package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;

public class Help extends Command
{
	protected Help()
	{
		command = "/help";
		help = "/help - showing help";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		String help = "";
		
		for (Command com : Commands.getCommandsList())
			help += com.help + "\n";
		
		for (Plugin p : PluginManager.plugins)
			for (Command com : p.commands)
				help += com.help + "\n";
		client.send(help);
	}
}