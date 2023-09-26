package com.serverd.command;

import java.util.ArrayList;
import java.util.Arrays;

import com.serverd.client.Client;
import com.serverd.client.processor.Processor;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.listener.ExecutionController;

/**
 * Command Processor class.
 * Command Processor is simple default processor to process commands with some default commands
 */
public class CommandProcessor extends Processor {
	
	/** Commands */
	protected static ArrayList<Command> commands = new ArrayList<>();
	
	static {
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

	public void receive(byte[] buffer) {	
		
		try {		
			if (currentCommand == null || !currentCommand.isRunned()) {
				String command_str = new String(buffer,0,buffer.length);
				printReceiveMessage(command_str);
				
				String[] command_raw = command_str.split(" ");
				String[] args = Arrays.copyOfRange(command_raw,1,command_raw.length);
				String command = command_raw[0];
				
				//execution controller
				boolean command_accepted = true;
				for (Plugin p : PluginManager.getPlugins()) {
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
				Command comm = getCommandByName(command);
				//search in plugins
				if (comm == null)
					for (Plugin p : PluginManager.getPlugins())
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
	 * @return Current command
	 */
	public Command getCurrentCommand() {
		return currentCommand;
	}
	
	
	/**
	 * Getting build in commands by name 
	 * @param name Command name
	 * @return Command object
	 */
	public static Command getCommandByName(String name) {
		for (Command command : commands)
			if (command.command.equals(name))
				return command;
		return null;
	}
	
	/**
	 * Get all build in commands
	 * @return Command {@link ArrayList}
	 */
	public static ArrayList<Command> getCommandsList() {
		return commands;
	}
}
