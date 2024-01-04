package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

public class Close extends Command {
	protected Close() {
		command = "/close";
		help = "/close <id> - close another client connection";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException {
		if (checkArgs(args,client, 1)) {
			int closeId = Integer.parseInt(args[0]);
			
			if (client.getClientManager().getClient(closeId) != null) {
				client.getClientManager().delete(closeId);
				send(client,ok());	
			} 
			else send(client,"ERROR client not found");
		}
	}
}