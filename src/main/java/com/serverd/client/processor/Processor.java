package com.serverd.client.processor;

import com.serverd.client.Client;

public abstract class Processor {
	
	/** Client*/
	protected Client client;
	
	/** Is supporting joining */
	protected boolean isSupportingJoining;
	
	/**
	 * Processor class constructor
	 * @param client Client instance
	 */
	public Processor(Client client,boolean isSupportingJoining) {
		this.client = client;
		this.isSupportingJoining = isSupportingJoining;
	}
	
	/**
	 * Processing byte message
	 * @param buffer Byte buffer to process
	 */
	public abstract void processCommand(byte[] buffer);
	
	/**
	 * Printing receive message
	 * @param message Receive message
	 */
	public void printReceiveMessage(String message) {}
	
	/**
	 * Printing send message
	 * @param message Send message
	 */
	public void printSendMessage(String message) {}

	public boolean isSupportedJoining() {
		return isSupportingJoining;
	}
}
