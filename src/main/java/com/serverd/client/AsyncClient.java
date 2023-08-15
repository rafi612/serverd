package com.serverd.client;

import java.nio.ByteBuffer;

public class AsyncClient extends Client {
	
	protected ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER);
	protected ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER);
	
	public Runnable afterReceive;
	boolean readPending = false;

	@FunctionalInterface
	public interface ReceiveComplete {
		void receiveDone(byte[] bytes);
	}

	public AsyncClient(int id) {
		super(id);
	}
	
	public void receive(ReceiveComplete handler) {}
	
	public Runnable getAfterReceive() {
		return afterReceive;
	}

	public void setAfterReceive(Runnable afterReceive) {
		this.afterReceive = afterReceive;
	}
	
	public void invokeReceive() {
		if (readPending) 
			return;
		afterReceive.run();
	}
	
	public void unlockRead() {
//		log.debug("Unlocked");
		
		if (!readPending)
			invokeReceive();
	}
	
	public void lockRead() {
//		log.debug("Locked");
	}
}
