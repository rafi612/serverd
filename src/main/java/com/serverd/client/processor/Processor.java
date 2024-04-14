package com.serverd.client.processor;

import com.serverd.app.ServerdApplication;
import com.serverd.client.Client;
import com.serverd.log.Log;

/**
 * Processor is used to process messages received by clients.
 * Each instance is assigned to a client. Instances are created by <code>{@link ProcessorFactory}</code>.
 */
public abstract class Processor {

	private static final Log log = Log.get(Processor.class);

	/** Client*/
	protected Client client;

	/** App context*/
	protected ServerdApplication app;
	
	/**
	 * Processor class constructor.
	 * @param client Client instance.
	 */
	public Processor(Client client) {
		this.client = client;
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
		log.info("Client " + client.getId() + " has been closed");
	}

	/**
	 * Handling exception thrown by client.
	 * @param exception Exception to handle.
	 */
	public void handleError(Exception exception) {
		client.log().error("Client " + client.getId() + " crashed: " + exception.getMessage());

		client.closeClient();
		client.getClientManager().delete(client.getId());
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
	 * Returns application instance
	 */
	public ServerdApplication getApp() {
		return app;
	}

	public void setApp(ServerdApplication app) {
		this.app = app;
	}
}
