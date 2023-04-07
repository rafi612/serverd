package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;

public class PluginCommand extends Command
{
	protected PluginCommand()
	{
		command = "/plugin";
		help = "/plugin <enable|disable|info> <filename> - manage installed plugins";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		if (checkArgs(args,client, 2))
		{
			Plugin p = PluginManager.getByFileName(args[1]);
			
			if (p == null)
			{
				client.send(error("Not found"));
			}
			else if (args[0].equals("enable"))
			{
				
				if (p.isRunned())
				{
					client.send(error("Plugin is already runned"));
				}
				else
				{
					if (PluginManager.enablePlugin(p))
						client.send(ok());
					else
						client.send(error("Plugin load failed"));
				}
			}
			else if (args[0].equals("disable"))
			{
				if (!p.isRunned())
				{
					client.send(error("Plugin is already stopped"));
				}
				else 
				{
					PluginManager.disablePlugin(p);
					client.send(ok());
				}
			}
			else if (args[0].equals("info"))
			{
				String message = "=============\n" + args[1] + ":\n=============\n" + p.getInfo().toString();
				client.send(message);
			}
		}
	}
}