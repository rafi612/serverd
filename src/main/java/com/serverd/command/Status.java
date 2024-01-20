package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

public class Status extends Command {
	protected Status() {
		command = "/status";
		help = "/status - shows status of all clients";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException {
		String message = client.getClientManager().getClientConnectedAmount() == 0 ? "No clients connected" : "";
		
		for (Client c : client.getClientManager().getAllClients())
			message += c.getName() 
				+ ": ID:" + c.getID()
				+ " Connected:" + c.isConnected()
				+ " Joined:" + c.getJoinedID()
				+ " Protocol:" + c.getProtocol().getName() 
				+ " IP:" + c.getIP() + ":" + c.getPort() + "\n";
		
		send(client,message);
	}
}
