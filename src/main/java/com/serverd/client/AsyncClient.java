package com.serverd.client;

import java.nio.ByteBuffer;

public class AsyncClient extends Client {
	
	protected ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER);
	protected ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER);
	
	public Runnable afterReceive;
	boolean readPending = false,locked = false;

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
		if (readPending) {
			log.warn("read pending... return");
			return;
		}
		afterReceive.run();
	}
	
	public void unlockRead() {
		
		log.debug("Unlocked");
		locked = false;
		
		if (!readPending)
			invokeReceive();
	}
	
	public void lockRead() {
		log.debug("Locked");
		locked = true;
	}
	
	public void processQueue() {		
		processSend(writeBuffer);
	}
	
	protected void queueBuffer(byte[] buf) {
		writeBuffer.clear();
		writeBuffer.put(buf);
		writeBuffer.flip();
		
		if (isJoined())
			getJoiner().lockRead();
		
	}
	
	public void processSend(ByteBuffer buffer) {
		
	}

}
