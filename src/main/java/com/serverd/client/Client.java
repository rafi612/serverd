package com.serverd.client;

import java.io.IOException;

import com.serverd.app.ServerdApplication;
import com.serverd.client.processor.Processor;
import com.serverd.command.CommandProcessor;
import com.serverd.log.Log;

/**
 * Representation of connected client.
 * This class is meant to be extended by specific client implementations.
 */
public class Client {
	
	private final int id;
	
	/** Connected */
	protected boolean connected;
	
	private int joinedId = -1;
	
	/** Max buffer size */
	public static final int BUFFER = 65536;
	
	private String name;

	/** Logger */
	protected final Log log;
	
	private boolean onceJoin = false;
	
	/** Processor */
	protected Processor processor = new CommandProcessor(this);

	private final ClientManager clientManager;
	
	/**
	 * Send continuation interface. Invoked when client send complete.
	 */
	@FunctionalInterface
	public interface SendContinuation {
		void invoke() throws IOException;
	}

	/**
	 * Client type.
	 */
	public enum Type {
		SENDER,RECEIVER,NONE;
	}

	/**
	 * Logger for client.
	 */
	protected class ClientLog extends Log {
		public ClientLog(String name) {
			super(name);
		}

		@Override
		protected synchronized void log(String level,String color,String message) {
			super.log(level,color,"[Client " + id + "] " + message);
		}
	}
	
	/**
	 * Client protocol.
	 * Representing Client protocol. Default {@link Protocol#TCP} or {@link Protocol#UDP}. 
	 * Can be custom when set {@link Protocol#CUSTOM}. Custom protocol must have set name using {@link Protocol#setName setName} method.
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
	public Client(int id, ClientManager clientManager) {
		this.id = id;
		this.clientManager = clientManager;
		
		connected = true;
		name = "Client " + id;
		
		log = new ClientLog(getClass().getName());
	}
	
	/**
	 * Receiving raw data
	 * @return byte array of data
	 * @throws IOException when socket throw error
	 */
	protected byte[] receive() throws IOException {
		return new byte[BUFFER];
	}
	
	/**
	 * Sending string message without executing {@link SendContinuation}.
	 * @param message Message to send
	 * @throws IOException when socket throw error
	 */
	public void send(String message) throws IOException {
		send(message,() -> {});
	}
	
	/**
	 * Sending string message, when complete executing {@link SendContinuation}.
	 * @param message Message to send
	 * @param continuation Send continuation handler
	 * @throws IOException when socket throw error
	 */
	public void send(String message,SendContinuation continuation) throws IOException {}
	
	/**
	 * Sending byte array, without executing {@link SendContinuation}.
	 * @param bytes Byte array
	 * @throws IOException when socket throw error
	 */
	public void send(byte[] bytes) throws IOException {
		send(bytes,() -> {});
	}
	
	/**
	 * Sending byte array, when complete executing {@link SendContinuation}.
	 * @param bytes Byte array
	 * @param continuation Send continuation handler
	 * @throws IOException when socket throw error
	 */
	public void send(byte[] bytes, SendContinuation continuation) throws IOException {}
	
	/**
	 * Closing client (may close sockets etc.)
	 */
	public void closeClient() {
		connected = false;
	}
	
	/**
	 * Returns client's IP
	 */
	public String getIP() {
		return "";
	}
	
	/**
	 * Returns client's port
	 */
	public int getPort() {
		return 0;
	}
	
	/**
	 * Returns true if client is connected
	 */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * Returns true if client is joined
	 */
	public boolean isJoined() {
		return joinedId != -1;
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
	 * Returns client joiner object.
	 */
	public Client getJoiner() {
		return clientManager.getClient(getJoinedID());
	}
	
	/**
	 * Locks client reading. 
	 * Lock and Unlock is mechanism to control data flow in most async clients (NIO and NIO2)
	 * @see Client#unlockRead unlockRead
	 */
	public void lockRead() {}
	
	/**
	 * Unlocks client reading. 
	 * Lock and Unlock is mechanism to control data flow in most async clients (NIO and NIO2)
	 * @see Client#lockRead lockRead
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
	 * Returns client's joined ID
	 */
	public int getJoinedID() {
		return joinedId;
	}
	
	/**
	 * Returns client's ID
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Returns client's name
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
	 */
	public Protocol getProtocol() {
		return protocol;
	}
	
	/**
	 * Returns client type enum
	 */
	public Type getType() {
		return type;
	}
	
	
	/**
	 * Returns client processor
	 * @see Processor
	 */
	public Processor getProcessor() {
		return processor;
	}

	/**
	 * Setting client processor
	 * @param processor Client new processor
	 * @see Processor
	 */
	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	/**
	 * Returns client manager of this client.
	 */
	public ClientManager getClientManager() {
		return clientManager;
	}

	public ServerdApplication getApp() {
		return clientManager.getApp();
	}
	
	/**
	 * Converts byte buffer to String message
	 * @param buffer Byte buffer
	 * @return String message
	 */
	public String toMessage(byte[] buffer) {
		return new String(buffer);
	}

	/**
	 * Returns client logger object.
	 */
	public Log log() {
		return log;
	}
	
	/**
	 * Join exception
	 */
	public static class JoinException extends Exception {
		private static final long serialVersionUID = 1L;
		/**
		 * JoinException class constructor
		 * @param message Message
		 */
		public JoinException(String message) {
			super(message);
		}
	}
	
	/**
	 * Joining to another client
	 * @param joinId Client ID to join
	 * @throws JoinException when join error occur 
	 */
	public void join(int joinId) throws JoinException {
		Client cl = clientManager.getClient(joinId);
		
		if (cl == null)
			throw new JoinException("Wrong client ID");
		
		if (isJoined())
			throw new JoinException("Client already joined");
		
		joinedId = joinId;
		type = Type.SENDER;
		
		cl.joinedId = id;
		cl.type = Type.RECEIVER;
	}
	
	/**
	 * Unjoining client
	 */
	public void unjoin() {
		Client cl = clientManager.getClient(joinedId);
		
		if (cl == null)
			return;
		
		cl.joinedId = -1;
		cl.type = Type.NONE;
		
		joinedId = -1;
		type = Type.NONE;
	}
	
	/**
	 * Join once to client, after receive response, 
	 * client will disconnect automatically (used by <b>/to</b> command)
	 * @param joinId Client ID to join once
	 * @throws JoinException when join error occur 
	 */
	public void onceJoin(int joinId) throws JoinException {
		if (joinId == id)
			throw new JoinException("can't join to self");
		
		onceJoin = true;
		
		join(joinId);
	}
	
	/**
	 * Invoking exception handler. Can be invoked when client occurs exception.
	 * @param exception Exception
	 */
	public void crash(Exception exception) {
		processor.handleError(exception);
	}	
}
