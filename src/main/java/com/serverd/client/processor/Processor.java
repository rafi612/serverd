package com.serverd.client.processor;

import com.serverd.app.ServerdApplication;
import com.serverd.client.Client;
import com.serverd.log.Log;

/**
 * Processor is used to process messages received by clients.
 * Each instance is assigned to a client. Instances are created by <code>{@link ProcessorFactory}</code>.
 * It can support joining, i.e. redirecting each message to the selected client,
 * then adding the redirection yourself in the code. 
 * The rest is implemented top-down so that NIO clients know how to route messages.
 */
public abstract class Processor {

	private static final Log log = Log.get(Processor.class);

	/** Client*/
	protected Client client;
	
	/** Is supporting joining */
	protected boolean isSupportingJoining;

	/** App context*/
	protected ServerdApplication app;
	
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
	 * Executed when the client opens the connection.
	 */
	public void onOpen() {}
	
	/**
	 * Processing received byte message.
	 * @param buffer Byte buffer to process.
	 */
	public abstract void receive(byte[] buffer);

	/**
	 * Executed when the client closes the connection.
	 */
	public void onClose() {
		log.info("Client " + client.getID() + " has been closed");
	}

	/**
	 * Handling exception thrown by client.
	 * @param exception Exception to handle.
	 */
	public void handleError(Exception exception) {
		if (client.isJoined())
			client.unjoin();

		client.log().error("Client " + client.getID() + " crashed: " + exception.getMessage());

		client.closeClient();
		client.getClientManager().delete(client.getID());
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
	 * Check if processor supporting joining client.
	 * @return true if processor supporting joining.
	 */
	public boolean isSupportedJoining() {
		return isSupportingJoining;
	}

	public ServerdApplication getApp() {
		return app;
	}

	public void setApp(ServerdApplication app) {
		this.app = app;
	}
}
