package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

public class Disconnect extends Command {
	protected Disconnect() {
		command = "/disconnect";
		help = "/disconnect - disconnect client";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException {
		client.closeClient();
		client.getClientManager().delete(client.getID());
		
		done();
	}
}
