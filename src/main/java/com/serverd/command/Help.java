package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

public class Help extends Command {
	protected Help() {
		command = "/help";
		help = "/help - showing help";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException {
		String help = "";
		
		for (Command com : CommandProcessor.getCommandsList())
			help += com.help + "\n";
		
		for (Plugin p : client.getApp().getPluginManager().getPlugins())
			for (Command com : p.commands)
				help += com.help + "\n";
		send(client,help);
	}
}