package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

public class Setname extends Command {
	protected Setname() {
		command = "/setname";
		help = "/setname <name> - setting name";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException {
		if (args.length < 1) 
			send(client,"Missing Argument");
		else {					
			client.setName(String.join(" ", args));
			
			send(client,ok());
		}
	}
}