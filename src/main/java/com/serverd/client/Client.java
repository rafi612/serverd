package com.serverd.client;

import java.io.IOException;
import com.serverd.client.processor.CommandProcessor;
import com.serverd.client.processor.Processor;
import com.serverd.log.Log;

/**
 * Client class
 */
public class Client {
	
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
	
	/** Processor */
	protected Processor processor = new CommandProcessor(this);

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
	}
	
	/**
	 * Receiving raw data
	 * @return byte array of data
	 * @throws IOException when socket throw error
	 */
	protected byte[] rawdataReceive() throws IOException {
		return new byte[BUFFER];
	}
	
	public void send(String mess) throws IOException {
		send(mess,() -> {});
	}
	
	/**
	 * Sending message
	 * @param mess Message to send
	 * @throws IOException when socket throw error
	 */
	public void send(String mess,Runnable continuation) throws IOException {}
	
	public void rawdataSend(byte[] bytes) throws IOException {
		rawdataSend(bytes,() -> {});
	}
	
	/**
	 * Sending raw data
	 * @param bytes Byte array
	 * @throws IOException when socket throw error
	 */
	public void rawdataSend(byte[] bytes,Runnable continuation) throws IOException {}
	
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
	 * Check if client is async (Using Java NIO2)
	 * @return true if client is selectable
	 */
	public boolean isAsync() {
		return this instanceof AsyncClient;
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
	 * Returns client processor
	 * @return Client processor
	 */
	public Processor getProcessor() {
		return processor;
	}

	/**
	 * Setting client processor
	 * @param processor Client new processor
	 */
	public void setProcessor(Processor processor) {
		this.processor = processor;
	}
	
	/**
	 * Converts byte buffer to String message
	 * @param buffer Byte buffer
	 * @return String message
	 */
	public String toMessage(byte[] buffer) {
		return new String(buffer,0,buffer.length);
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
	public void crash(Exception exception) {
		if (!crashed && connected) {
			if (isJoined())
				unjoin();
			
			exception.printStackTrace();
			
			crashed = true;
			log.error("Client " + id + " crashed: " + exception.getMessage());
			
			closeClient();
			ClientManager.delete(id);
		}
	}	
}
