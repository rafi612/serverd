package com.serverd.client.processor;

import java.util.Arrays;

import com.serverd.client.Client;
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
			if (currentCommand == null || !currentCommand.isRunned()) {
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
				
				Command cmd = null;
				//clone command object
				if (comm != null)
					cmd = (Command) comm.clone();
				
				if (cmd == null) {
					if (client.getJoinedID() == -1)
						client.send(Codes.unknownCommand());
					else {
						if (client.isOnceJoined())
							client.unjoin();
						
						client.getJoiner().send(command_str);
					} 
				} else {
					currentCommand = cmd;
					cmd.setRunned(true);
					cmd.execute(args, client, plugin);
				}
			} else {
				currentCommand.processReceive(buffer);
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
