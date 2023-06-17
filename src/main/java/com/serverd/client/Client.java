package com.serverd.client;

import java.io.IOException;
import java.util.Arrays;

import com.serverd.command.Command;
import com.serverd.command.Commands;
import com.serverd.command.Codes;
import com.serverd.log.Log;
import com.serverd.plugin.Encoder;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.listener.ExecutionController;

/**
 * Client class
 */
public class Client implements Runnable {
	/** Client's thread */
 	protected Thread thread;
	
	private int id;
	
	/** Connected */
	protected boolean connected;
	private boolean crashed = false;
	
	private int joinedid = -1;
	
	/** Max buffer size */
	public static final int BUFFER = 65536;
	
	private String name;
	
	/** Logger */
	public Log log;
	
	private boolean onceJoin = false;
	
	/** Client's encoder*/
	protected Encoder encoder;
	
	private Command currentCommand;
	
	/**
	 * Client type
	 */
	public enum Type {
		SENDER,RECEIVER,NONE;
	}
	
	/**
	 * Client protocol
	 */
	public enum Protocol {
		TCP("TCP"),UDP("UDP"),CUSTOM("");
		
		public String name;
		
		Protocol(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
	}
	
	/** Client's type*/
	protected Type type = Type.NONE;
	/** Client's protocol*/
	protected Protocol protocol;
	
	/**
	 * Client class constructor
	 * @param id Client ID
	 */
	public Client(int id) {
		this.id = id;
		
		connected = true;
		name = "Client " + id;
		
		log = new Log("Client " + id);
		
		encoder = new Encoder();
	}
	
	/**
	 * Setting encoder on client
	 * @param encoder {@link Encoder} instance
	 */
	public void setEncoder(Encoder encoder) {
		this.encoder = encoder;
	}
	
	/**
	 * Returns client encoder
	 * @return Client's {@link Encoder}
	 */
	public Encoder getEncoder() {
		return encoder;
	}
	
	/**
	 * Receiving message
	 * @return Received message
	 * @throws IOException when socket throw error
	 */
	protected byte[] rawdataReceive() throws IOException {
		return new byte[BUFFER];
	}
	
	/**
	 * Sending message
	 * @param mess Message to send
	 * @throws IOException when socket throw error
	 */
	public void send(String mess) throws IOException {}
	
	/**
	 * Sending raw data
	 * @param bytes Byte array
	 * @throws IOException when socket throw error
	 */
	public void rawdataSend(byte[] bytes) throws IOException {}
	
	/**
	 * Converts byte buffer to String message
	 * @param buffer Byte buffer
	 * @return String message
	 */
	public String toMessage(byte[] buffer) {
		String message = encoder.decode(new String(buffer,0,buffer.length), this);
		
		log.info("<Reveived> " + message);
		
		return message;
	}
	
	/**
	 * Closing socket
	 */
	public void closeClient() {
		connected = false;
	}
	
	/**
	 * Returns client ID
	 * @return Client's IP
	 */
	public String getIP() {
		return "";
	}
	
	/**
	 * Returns client connected port
	 * @return Client's port
	 */
	public int getPort() {
		return 0;
	}
	
	/**
	 * Returns client connection state
	 * @return true if client is connected
	 */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * Returns client joined state
	 * @return true if client is joined
	 */
	public boolean isJoined() {
		return joinedid != -1;
	}
	
	/**
	 * Check if client is selectable (Using Java NIO)
	 * @return true if client is selectable
	 */
	public boolean isSelectable() {
		return this instanceof SelectableClient;
	}
	
	/**
	 * Returns client joiner object
	 * @return client joiner object.
	 */
	public Client getJoiner() {
		return ClientManager.getClient(getJoinedID());
	}
	
	/**
	 * Locks client reading (Usually to prevent buffer overflow in Java NIO selectors)
	 * @see #unlockRead
	 */
	public void lockRead() {}
	
	/**
	 * Unlocks client reading
	 * @see #lockRead
	 */
	public void unlockRead() {}
	
	/**
	 * State of once join
	 * @return true if client is once joined
	 * @see Client#onceJoin
	 */
	public boolean isOnceJoined() {
		return onceJoin;
	}
	
	/**
	 * Returns the ID of the client that is joined
	 * @return Client's joined ID
	 */
	public int getJoinedID() {
		return joinedid;
	}
	
	/**
	 * Returns client ID
	 * @return Client's ID 
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Returns client name
	 * @return Client's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Setting client name
	 * @param name Client new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns client protocol enum
	 * @return Client protocol enum
	 */
	public Protocol getProtocol() {
		return protocol;
	}
	
	/**
	 * Returns client type enum
	 * @return Client type enum
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * Returns current command
	 * @return Current command
	 */
	public Command getCurrentCommand() {
		return currentCommand;
	}
	
	/**
	 * Join exception
	 */
	public class JoinException extends Exception {
		private static final long serialVersionUID = 1L;
		/**
		 * JoinException class constructor
		 * @param message Message
		 */
		public JoinException(String message)
		{
			super(message);
		}
	}
	
	/**
	 * Joining to another client
	 * @param joinid Client ID to join
	 * @throws JoinException when join error occur 
	 */
	public void join(int joinid) throws JoinException {		
		Client cl = ClientManager.getClient(joinid);
		
		if (cl == null)
			throw new JoinException("wrong client ID");
		
		if (isJoined())
			throw new JoinException("client already joined");
		
		joinedid = joinid;
		type = Type.SENDER;
		
		cl.joinedid = id;
		cl.type = Type.RECEIVER;
	}
	
	/**
	 * Unjoining client
	 */
	public void unjoin() {
		Client cl = ClientManager.getClient(joinedid);
		
		if (cl == null)
			return;
		
		cl.joinedid = -1;
		cl.type = Type.NONE;
		
		joinedid = -1;
		type = Type.NONE;
	}
	
	/**
	 * Join once to client, after receive response, 
	 * client will disconnect automatically (used by <b>/to</b> command)
	 * @param joinid Client ID to join once
	 * @throws JoinException when join error occur 
	 */
	public void onceJoin(int joinid) throws JoinException {
		if (joinid == id)
			throw new JoinException("can't join to self");
		
		onceJoin = true;
		
		join(joinid);
	}
	
	/**
	 * Crash handler
	 * @param exception Exception
	 */
	protected void crash(Exception exception) {
		if (!crashed && connected) {
			if (isJoined())
				unjoin();
			
			crashed = true;
			log.error("Client " + id + " crashed: " + exception.getMessage());
			
			closeClient();
			ClientManager.delete(id);
		}
	}
	

	@Override
	public void run() {
		try {
			while (connected) {
				byte[] bytes = rawdataReceive();
				if (bytes != null)
					processCommand(bytes);
			}
		} catch (IOException e) {
			crash(e);
		}
	}
	
	
	/**
	 * Processing command
	 * @param buffer Byte buffer to process
	 */
	public void processCommand(byte[] buffer) {	
		try {
			if (currentCommand == null) {
				String command_str = toMessage(buffer);
				
				String[] command_raw = command_str.split(" ");
				String[] args = Arrays.copyOfRange(command_raw,1,command_raw.length);
				String command = command_raw[0];
				
				//execution controller
				boolean command_accepted = true;
				for (Plugin p : PluginManager.plugins) {
					if (!command_accepted)
						break;
					
					for (ExecutionController e : p.executionControllers) {
						if (!e.controlCommand(command, args, this, p)) {
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
					if (joinedid == -1)
						send(Codes.unknownCommand());
					else {
						ClientManager.clients.get(joinedid).send(command_str);
						
						if (onceJoin)
							unjoin();
					} 
				} else {
					currentCommand = comm;
					comm.runned = true;
					comm.execute(args, this, plugin);
					
					if (!currentCommand.isStayAlive())
						currentCommand = null;
				}
			} else {
				if (currentCommand.isStayAlive() && currentCommand.isRunned())
					currentCommand.processReceive(buffer,this);
				
				if (!currentCommand.isStayAlive())
					currentCommand = null;
			}
		} catch (Exception e) {
			crash(e);
		}
	}
}
