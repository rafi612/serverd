package com.serverd.client;

import java.nio.ByteBuffer;

/**
 * Class representing asynchronous clients.
 * Primary used by NIO2 client implementations.
 */
public class AsyncClient extends Client {
	
	/** Write Buffer */
	protected ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER);
	/** Receive Buffer */
	protected ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER);
	
	/** Read pending */
	protected boolean readPending = false;
	
	private Runnable afterReceive;

	/**
	 * Receive complete interface. Invoked when client receive complete.
	 */
	@FunctionalInterface
	public interface ReceiveComplete {
		void receiveDone(byte[] bytes);
	}

	/**
     * Async Client Constructor.
     * @param id Client ID.
     * @param clientManager Client manager instance.
     */
	public AsyncClient(int id, ClientManager clientManager) {
		super(id,clientManager);
	}
	
	/**
	 * Receive message and invoke handler after.
	 * @param handler ReceiveComplete object.
	 */
	public void receive(ReceiveComplete handler) {}
	

	/**
	 * Returns after receive handler.
	 */
	public Runnable getAfterReceive() {
		return afterReceive;
	}

	/**
	 * Setting after receive handler.
	 * @param afterReceive After receive handler.
	 */
	public void setAfterReceive(Runnable afterReceive) {
		this.afterReceive = afterReceive;
	}
	
	/**
	 * Invoking after receive handler to receive message after processing earlier message. 
	 */
	public void invokeReceive() {
		afterReceive.run();
	}
	
	@Override
	public void unlockRead() {
		if (!readPending)
			invokeReceive();
	}
}
