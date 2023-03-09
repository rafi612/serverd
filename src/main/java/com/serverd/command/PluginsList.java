package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;

public class PluginsList extends Command
{
	protected PluginsList()
	{
		command = "/plugins-list";
		help = "/plugins-list - list of loaded plugins";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		String message = "";
		String[] pluginNames = PluginManager.listPluginsName();
		
		message += "Plugins installed:\n";
		
		if (pluginNames.length > 0) 
		{
			for (String s : pluginNames) 
			{
				message += s + "\tEnable:" + PluginManager.getByFileName(s).isRunned() + "\n";
			}
			client.send(message);
		}
		else client.send("No plugins installed");
	}
}