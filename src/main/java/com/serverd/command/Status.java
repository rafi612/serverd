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

		CommandProcessor processor = (CommandProcessor) client.getProcessor();
		
		for (Client c : client.getClientManager().getAllClients())
			message += c.getName() 
				+ ": ID:" + c.getId()
				+ " Connected:" + c.isConnected()
				+ " Joined:" + processor.getJoinedID()
				+ " Protocol:" + c.getProtocol().getName() 
				+ " IP:" + c.getIp() + ":" + c.getPort() + "\n";
		
		send(client,message);
	}
}
