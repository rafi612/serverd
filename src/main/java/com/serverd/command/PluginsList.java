package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

public class PluginsList extends Command {
	protected PluginsList() {
		command = "/plugins-list";
		help = "/plugins-list - list of loaded plugins";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException {
		String[] pluginNames = client.getApp().getPluginManager().listPluginsName();
		
		if (pluginNames.length > 0) {
			String message = "Plugins installed:\n";
			for (String name : pluginNames) 
				message += name + "\tEnable:" + client.getApp().getPluginManager().getByFileName(name).isRunning() + "\n";
			send(client,message);
		}
		else send(client,"No plugins installed");
	}
}
