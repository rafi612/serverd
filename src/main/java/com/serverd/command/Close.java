package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.client.ClientManager;
import com.serverd.plugin.Plugin;

public class Close extends Command
{
	protected Close()
	{
		command = "/close";
		help = "/close <id> - close another client connection";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		if (checkArgs(args,client, 1) == 0)
		{
			int closeid = Integer.parseInt(args[0]);
			
			if (ClientManager.getClient(closeid) != null)
			{
				ClientManager.delete(closeid);
				client.send(ok());	
			}
			else client.send("ERROR client not found");
		}
	}
}