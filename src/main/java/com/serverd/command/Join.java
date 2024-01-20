package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.command.CommandProcessor.JoinException;
import com.serverd.plugin.Plugin;

public class Join extends Command {
	protected Join() {
		command = "/join";
		help = "/join <id> - join to client";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException {

		CommandProcessor processor = (CommandProcessor) client.getProcessor();

		if (checkArgs(args,client,1)) {
			try {
				processor.join(Integer.parseInt(args[0]));
				send(client,ok());
			}
			catch (JoinException e) {
				send(client,error(e.getMessage()));
			}	
		}
	}
}