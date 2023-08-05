package com.serverd.client.processor;

import com.serverd.client.Client;

public abstract class Processor {
	
	protected Client client;
	
	public Processor(Client client) {
		this.client = client;
	}
	
	/**
	 * Processing byte message
	 * @param buffer Byte buffer to process
	 */
	public abstract void processCommand(byte[] buffer);
	
	public void printReceiveMessage(String message) {}
	
	public void printSendMessage(String message) {}
}
