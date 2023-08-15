package com.serverd.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public abstract class SelectableClient extends Client 
{
	/** Selector */
	protected Selector selector;
	
	protected ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER);
	protected ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER);
	
	protected boolean readyToWrite;
	protected long lastReadTime;
	
	protected Runnable sendContinuation;
	
	/**
	 * NonBlockingClient constructor
	 * @param id client ID
	 * @param selector client Selector object
	 */
	public SelectableClient(int id,Selector selector) {
		super(id);
		this.selector = selector;
		
		updateTimeout();
	}
	
	protected void updateTimeout() {
		lastReadTime = System.currentTimeMillis();
	}
	
	public long lastReadTime() {
		return lastReadTime;
	}
	
	protected void queueBuffer(byte[] buf) {
		writeBuffer.put(buf);
		writeBuffer.flip();
		
		readyToWrite = true;
		
		if (isJoined())
			getJoiner().lockRead();
	}
	
	@Override
	public void lockRead() {
		SelectionKey key = getKey();
		key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
		selector.wakeup();
	}
	
	@Override
	public void unlockRead() {
		SelectionKey key = getKey();
		key.interestOps(key.interestOps() | SelectionKey.OP_READ);
		selector.wakeup();
	}
	
	public boolean isReadyToWrite() {
		return readyToWrite;
	}

	public boolean processQueue() throws IOException {
		processSend(writeBuffer);
		
		if (writeBuffer.remaining() == 0)
		{
			writeBuffer.clear();
			readyToWrite = false;
			return true;
		}
		else return false;
	}
	
	public abstract SelectionKey getKey();
	public abstract long processSend(ByteBuffer buffer) throws IOException;

}
