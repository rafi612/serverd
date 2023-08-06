package com.serverd.client.processor;

import java.util.Arrays;

import com.serverd.client.Client;
import com.serverd.client.ClientManager;
import com.serverd.command.Codes;
import com.serverd.command.Command;
import com.serverd.command.Commands;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.listener.ExecutionController;

public class CommandProcessor extends Processor {

	private Command currentCommand;
	
	public CommandProcessor(Client client) {
		super(client,true);
	}

	public void printReceiveMessage(String message) {
		client.log.info("<Reveived> " + message);
	}
	
	public void printSendMessage(String message) {
		client.log.info("<Sended> " + message);
	}

	public void processCommand(byte[] buffer) {	
		try {
			if (currentCommand == null) {
				String command_str = new String(buffer,0,buffer.length);
				printReceiveMessage(command_str);
				
				String[] command_raw = command_str.split(" ");
				String[] args = Arrays.copyOfRange(command_raw,1,command_raw.length);
				String command = command_raw[0];
				
				//execution controller
				boolean command_accepted = true;
				for (Plugin p : PluginManager.plugins) {
					if (!command_accepted)
						break;
					
					for (ExecutionController e : p.executionControllers) {
						if (!e.controlCommand(command, args, client, p)) {
							command_accepted = false;
							break;
						}
					}
				}

				if (!command_accepted)
					return;
				
				Plugin plugin = null;
				
				//search in base
				Command comm = Commands.getByName(command);
				//search in plugins
				if (comm == null)
					for (Plugin p : PluginManager.plugins)
						for (Command c : p.commands)
							if (command.equals(c.command)) {
								plugin = p;
								comm = c;
							}
				
				//copy command object
				if (comm != null)
					comm = (Command) comm.clone();
				
				if (comm == null) {
					if (client.getJoinedID() == -1)
						client.send(Codes.unknownCommand());
					else {
						ClientManager.clients.get(client.getJoinedID()).send(command_str);
						
						if (client.isOnceJoined())
							client.unjoin();
					} 
				} else {
					currentCommand = comm;
					comm.runned = true;
					comm.execute(args, client, plugin);
					
					if (!currentCommand.isStayAlive())
						currentCommand = null;
				}
			} else {
				if (currentCommand.isStayAlive() && currentCommand.isRunned())
					currentCommand.processReceive(buffer,client);
				
				if (!currentCommand.isStayAlive())
					currentCommand = null;
			}
		} catch (Exception e) {
			client.crash(e);
		}
	}
	
	/**
	 * Returns current command
	 * @return Current command
	 */
	public Command getCurrentCommand() {
		return currentCommand;
	}

}
