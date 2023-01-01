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
		String message = ClientManager.getClientConnectedAmount() == 0 ? "No clients connected" : "";
		
		for (Client c : ClientManager.getAllClients()) 
			message += client.getName() 
				+ ": ID:" + c.getID()
				+ " Connected:" + c.isConnected()
				+ " Joined:" + c.getJoinedID() 
				+ " Type:" + c.getType().toString() 
				+ " Protocol:" + c.getProtocol().getName() 
				+ " IP:" + c.getIP() + ":" + c.getPort() + "\n";
		
		client.send(message);
	}
}