package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

public class Id extends Command
{
	protected Id()
	{
		command = "/id";
		help = "/id - shows id";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		client.send(String.valueOf(client.getID()));
	}
}