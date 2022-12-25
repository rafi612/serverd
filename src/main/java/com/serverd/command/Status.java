package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.client.ClientManager;
import com.serverd.plugin.Plugin;

public class Status extends Command
{
	public Status()
	{
		command = "/status";
		help = "/status - shows status of all clients";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		client.send(ClientManager.statusall());
	}
}