package com.serverd.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.serverd.client.Client;
import com.serverd.client.Client.JoinException;
import com.serverd.client.ClientManager;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;

public class Commands 
{
	public static ArrayList<Command> commands = new ArrayList<>();
	public static void init()
	{
		commands.add(new Disconnect());
		commands.add(new Id());
		commands.add(new Status());
		commands.add(new To());
		commands.add(new Join());
		commands.add(new Close());
		commands.add(new Unjoin());
		commands.add(new Rawdata());
		commands.add(new Setname());
		commands.add(new PluginCommand());
		commands.add(new PluginsList());
		commands.add(new Help());
	}
	
	public static ArrayList<Command> getCommandsList()
	{
		return commands;
	}
	
	public static Command getByName(String name)
	{
		for (Command command : commands)
			if (command.command.equals(name))
				return command;
		return null;
	}
}

class Disconnect extends Command
{
	public Disconnect()
	{
		command = "/disconnect";
		help = "/disconnect - disconnect client";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		//unjoining
		if (client.isJoined())
			client.unjoin();
		
		client.closeClient();
		ClientManager.delete(client.getID());
	}
}


class Id extends Command
{
	public Id()
	{
		command = "/id";
		help = "/id - shows id";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		client.send(String.valueOf(client.getID()));
	}
}


class Status extends Command
{
	public Status()
	{
		command = "/status";
		help = "/status - shows status of all clients";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		client.send(ClientManager.statusall());
	}
}

class To extends Command
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
			client.send("Missing Argument");
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
				client.send(e.getMessage());
			}
		}
	}
}

class Join extends Command
{
	public Join()
	{
		command = "/join";
		help = "/join <id> - join to client";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		if (checkArgs(args,client,1) == 0)
		{
			try
			{
				client.join(Integer.parseInt(args[0]));
				client.send("Joined, now you are in joined mode.");
			}
			catch (JoinException e)
			{
				client.send(e.getMessage());
			}	
		}
	}
}

class Close extends Command
{
	public Close()
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
			
			ClientManager.delete(closeid);
			client.send("Client " + closeid + " closed.");
		}
	}
}

class Unjoin extends Command
{
	public Unjoin()
	{
		command = "/unjoin";
		help = "/unjoin - unjoin current client";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		client.unjoin();
		client.send("Returning back to command mode");
	}
}

class Rawdata extends Command
{
	public Rawdata()
	{
		command = "/rawdata";
		help = "/rawdata <buffer> - run rawdata mode with buffer";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		if (checkArgs(args,client, 1) == 0)
		{
			client.send("Done");
			
			int buffersize = Integer.parseInt(args[0]);

            Client joined = ClientManager.clients.get(client.getJoinedID());
			
			if (client.getJoinedID() != -1)
			{
				client.log.info("Raw data mode started," + buffersize + " bytes can be sended");
				int i = 0;
				while (i < buffersize)
				{
					byte[] buffer = client.rawdataReceive(Client.BUFFER);
					joined.rawdataSend(buffer);

					i+= buffer.length;
				}
				client.send("Raw data mode closed");
			}
			else client.send("Error: you must be joined!");
		}
	}
}

class Setname extends Command
{
	public Setname()
	{
		command = "/setname";
		help = "/setname <name> - setting name";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		if (args.length < 1) 
			client.send("Missing Argument");
		else
		{					
			client.setName(String.join(" ", args));
			
			client.send("Name has been set to \"" + client.getName() + "\"");
		}
	}
}

class PluginCommand extends Command
{
	public PluginCommand()
	{
		command = "/plugin";
		help = "/plugin <enable|disable|info> <filename> - manage installed plugins";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		if (checkArgs(args,client, 2) == 0)
		{
			Plugin p = PluginManager.getByFileName(args[1]);
			
			if (p == null)
			{
				client.send("Plugin " + args[1] + " not found");
			}
			else if (args[0].equals("enable"))
			{
				
				if (p.isRunned())
				{
					client.send("Plugin " + args[1] + " is runned now");
				}
				else
				{
					if (PluginManager.enablePlugin(p))
						client.send("Plugin load succesfully");
					else
						client.send("Plugin load failed");
				}
			}
			else if (args[0].equals("disable"))
			{
				if (!p.isRunned())
				{
					client.send("Plugin " + args[1] + " is stopped now");
				}
				else 
				{
					PluginManager.disablePlugin(p);
					client.send("Plugin was disabled");
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

class PluginsList extends Command
{
	public PluginsList()
	{
		command = "/plugins-list";
		help = "/plugins-list - list of loaded plugins";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		String message = "";
		String[] pluginNames = PluginManager.listPluginsName();
		
		String absolutepath = new File(PluginManager.pluginDir).getCanonicalPath();
		
		message += "Plugins installed in: " + absolutepath + "\n";
		
		if (pluginNames.length > 0) 
		{
			for (String s : pluginNames) 
			{
				message += s + "\tEnable:" + PluginManager.getByFileName(s).isRunned() + "\n";
			}
			client.send(message);
		}
		else client.send("No plugins installed in " + absolutepath);
	}
}

class Help extends Command
{
	public Help()
	{
		command = "/help";
		help = "/help - showing help";
	}
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		String help = "";
		
		for (Command com : Commands.getCommandsList())
			help += com.help + "\n";
		
		for (Plugin p : PluginManager.plugins)
			for (Command com : p.commands)
				help += com.help + "\n";
		client.send(help);
	}
}
