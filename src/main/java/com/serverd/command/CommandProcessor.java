package com.serverd.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.serverd.client.Client;
import com.serverd.client.processor.Processor;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.listener.ExecutionController;

/**
 * Command Processor class.
 * Command Processor is simple default processor to process commands with some default commands.
 */
public class CommandProcessor extends Processor {
	
	/** Commands */
	protected static ArrayList<Command> commands = new ArrayList<>();

	private int joinedId = -1;
	
	static {
		commands.add(new Disconnect());
		commands.add(new Id());
		commands.add(new Status());
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
		super(client);
	}

	@Override
	public void printReceiveMessage(String message) {
		client.log().info("<Received> " + message);
	}
	@Override
	public void printSendMessage(String message) {
		client.log().info("<Sent> " + message);
	}

	public void receive(byte[] buffer) {
		try {		
			if (currentCommand == null || !currentCommand.isRunning()) {
				String command_str = new String(buffer);
				printReceiveMessage(command_str);
				
				String[] command_raw = command_str.split(" ");
				String[] args = Arrays.copyOfRange(command_raw,1,command_raw.length);
				String command = command_raw[0];
				
				//execution controller
				boolean command_accepted = true;
				for (Plugin p : client.getApp().getPluginManager().getPlugins()) {
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
					for (Plugin p : client.getApp().getPluginManager().getPlugins())
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
					if (getJoinedID() == -1)
						send(Codes.unknownCommand(),() -> {});
					else
						send(getJoiner(),command_str,() -> {});
				} else {
					currentCommand = cmd;
					cmd.setRunning(true);
					cmd.execute(args, client, plugin);
				}
			} else {
				currentCommand.processReceive(buffer);
			}
		} catch (Exception e) {
			client.crash(e);
		}
	}

	public void handleError(Exception exception) {
		if (isJoined())
			unjoin();

		super.handleError(exception);
	}

	public void onClose() {
		if (isJoined())
			unjoin();

		super.onClose();
	}

	/**
	 * Sending byte message to client.
	 * Wrapping {@link Client#send(byte[], Client.SendContinuation)} to properly handle locking.
	 * @param bytes Byte array
	 * @param continuation Send continuation handler
	 * @throws IOException when socket throw error.
	 */
	protected void send(byte[] bytes, Client.SendContinuation continuation) throws IOException {
		send(client,bytes,continuation);
	}

	/**
	 * Sending byte message to client.
	 * Wrapping {@link Client#send(String, Client.SendContinuation)} to properly handle locking.
	 * @param message String message
	 * @param continuation Send continuation handler
	 * @throws IOException when socket throw error.
	 */
	protected void send(String message, Client.SendContinuation continuation) throws IOException {
		send(client,message,continuation);
	}

	/**
	 * Sending byte message to client.
	 * Wrapping {@link Client#send(byte[], Client.SendContinuation)} to properly handle locking.
	 * @param client Client instance
	 * @param bytes Byte array
	 * @param continuation Send continuation handler
	 * @throws IOException when socket throw error.
	 */
	protected void send(Client client,byte[] bytes, Client.SendContinuation continuation) throws IOException {
		if (isJoined())
			getJoiner().lockRead();

		client.send(bytes,() -> {
			continuation.invoke();

			if (getJoiner() != null)
				getJoiner().unlockRead();
		});
	}

	/**
	 * Sending byte message to client.
	 * Wrapping {@link Client#send(String, Client.SendContinuation)} to properly handle locking.
	 * @param client Client instance
	 * @param message String message
	 * @param continuation Send continuation handler
	 * @throws IOException when socket throw error.
	 */
	protected void send(Client client,String message, Client.SendContinuation continuation) throws IOException {
		if (isJoined())
			getJoiner().lockRead();

		client.send(message,() -> {
			continuation.invoke();

			if (getJoiner() != null)
				getJoiner().unlockRead();
		});
	}

	/**
	 * Join exception
	 */
	public static class JoinException extends Exception {
		/**
		 * JoinException class constructor
		 * @param message Message
		 */
		public JoinException(String message) {
			super(message);
		}
	}

	/**
	 * Returns true if client is joined
	 */
	public boolean isJoined() {
		return joinedId != -1;
	}

	/**
	 * Returns client's joined ID
	 */
	public int getJoinedID() {
		return joinedId;
	}

	/**
	 * Returns client joiner object.
	 */
	public Client getJoiner() {
		return client.getClientManager().getClient(getJoinedID());
	}

	/**
	 * Joining to another client
	 * @param joinId Client ID to join
	 * @throws JoinException when join error occur
	 */
	public void join(int joinId) throws JoinException {
		Client cl = client.getClientManager().getClient(joinId);

		if (cl == null)
			throw new JoinException("Wrong client ID");

		if (isJoined())
			throw new JoinException("Client already joined");

		joinedId = joinId;

		CommandProcessor processor = (CommandProcessor) cl.getProcessor();

		processor.joinedId = client.getID();
	}

	/**
	 * Unjoining client
	 */
	public void unjoin() {
		Client cl = client.getClientManager().getClient(joinedId);

		if (cl == null)
			return;

		CommandProcessor processor = (CommandProcessor) cl.getProcessor();

		processor.joinedId = -1;
		joinedId = -1;
	}
	
	/**
	 * Returns current command.
	 */
	public Command getCurrentCommand() {
		return currentCommand;
	}
	
	
	/**
	 * Getting build-in commands by name.
	 * @param name Command name.
	 * @return Command object.
	 */
	public static Command getCommandByName(String name) {
		for (Command command : commands)
			if (command.command.equals(name))
				return command;
		return null;
	}
	
	/**
	 * Get all build-in commands.
	 * @return Command {@link ArrayList}.
	 */
	public static ArrayList<Command> getCommandsList() {
		return commands;
	}
}
