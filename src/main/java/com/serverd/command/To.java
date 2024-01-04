package com.serverd.command;

import java.io.IOException;
import java.util.Arrays;

import com.serverd.client.Client;
import com.serverd.client.Client.JoinException;
import com.serverd.plugin.Plugin;

public class To extends Command {
	protected To() {
		command = "/to";
		help = "/to <id> <command> - sending command without joining to client";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException  {
		if (args.length < 1) 
			send(client,error("Missing Argument"));
		else {
			String com = String.join(" ", Arrays.copyOfRange(args,1,args.length));
			int id = Integer.parseInt(args[0]);

			Client targetClient = client.getClientManager().getClient(id);
			try {
				client.onceJoin(id);
				send(targetClient,com);
			} catch (JoinException e) {
				send(client,error(e.getMessage()));
			}
		}
	}
}