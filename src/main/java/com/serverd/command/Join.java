package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.client.Client.JoinException;
import com.serverd.plugin.Plugin;

public class Join extends Command {
	protected Join() {
		command = "/join";
		help = "/join <id> - join to client";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException {
		if (checkArgs(args,client,1)) {
			try {
				client.join(Integer.parseInt(args[0]));
				client.send(ok());
			}
			catch (JoinException e) {
				client.send(error(e.getMessage()));
			}	
		}
	}
}