package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;

public class PluginsList extends Command {
	protected PluginsList() {
		command = "/plugins-list";
		help = "/plugins-list - list of loaded plugins";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException {
		String[] pluginNames = PluginManager.listPluginsName();
		
		if (pluginNames.length > 0) {
			String message = "Plugins installed:\n";
			for (String s : pluginNames) 
				message += s + "\tEnable:" + PluginManager.getByFileName(s).isRunned() + "\n";
			send(client,message);
		}
		else send(client,"No plugins installed");
	}
}
