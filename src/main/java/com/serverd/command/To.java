package com.serverd.command;

import java.io.IOException;
import java.util.Arrays;

import com.serverd.client.Client;
import com.serverd.client.ClientManager;
import com.serverd.client.Client.JoinException;
import com.serverd.plugin.Plugin;

public class To extends Command
{
	public To()
	{
		command = "/to";
		help = "/to <id> <command> - sending command without joining to client";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		if (args.length < 1) 
			client.send(error("Missing Argument"));
		else
		{
			String com = String.join(" ", Arrays.copyOfRange(args,1,args.length));
			int id = Integer.parseInt(args[0]);

			Client targetClient = ClientManager.getClient(id);
			try 
			{
				client.onceJoin(id);
				targetClient.send(com);
			}
			catch (JoinException e)
			{
				client.send(error(e.getMessage()));
			}
		}
	}
}