package com.serverd.client.processor;

import com.serverd.client.Client;
import com.serverd.client.ClientManager;
import com.serverd.log.Log;

/**
 * Processor is used to process messages received by clients.
 * Each instance is assigned to a client. it can support joining, i.e. redirecting each message to the selected client,
 * then adding the redirection yourself in the code. 
 * The rest is implemented top-down so that NIO clients know how to route messages.
 */
public abstract class Processor {
	/** Client*/
	protected Client client;
	
	/** Is supporting joining */
	protected boolean isSupportingJoining;
	
	/**
	 * Processor class constructor.
	 * @param client Client instance.
	 * @param isSupportingJoining is processor support joining clients?
	 */
	public Processor(Client client,boolean isSupportingJoining) {
		this.client = client;
		this.isSupportingJoining = isSupportingJoining;
	}
	
	/**
	 * Processing received byte message.
	 * @param buffer Byte buffer to process.
	 */
	public abstract void receive(byte[] buffer);

	/**
	 * Handling exception thrown by client.
	 * @param exception Exception to handle.
	 */
	public void handleError(Exception exception) {
		client.log.error("Client " + client.getID() + " crashed: " + exception.getMessage());
	}
	
	/**
	 * Printing receive message.
	 * @param message Receive message.
	 */
	public void printReceiveMessage(String message) {}
	
	/**
	 * Printing send message.
	 * @param message Send message.
	 */
	public void printSendMessage(String message) {}
	
	/**
	 * Printing message when client was deleted and connection closed.
	 * @param client Client instance.
	 * @param log Logger from upstream class (Recently {@link ClientManager}).
	 */
	public void printDeleteMessage(Client client,Log log) {}

	/**
	 * Check if processor supporting joining client.
	 * @return true if processor supporting joining.
	 */
	public boolean isSupportedJoining() {
		return isSupportingJoining;
	}
}
