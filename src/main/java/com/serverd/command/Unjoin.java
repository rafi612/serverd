package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

public class Unjoin extends Command {
	public Unjoin() {
		command = "/unjoin";
		help = "/unjoin - unjoin current client";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException  {
		client.unjoin();
		send(client,ok());
	}
}
