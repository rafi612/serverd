package com.serverd.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Selectable Client class. Used by NIO Clients
 */
public abstract class SelectableClient extends Client 
{
	/** Selector */
	protected Selector selector;
	
	/**Write Buffer*/
	protected ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER);
	/**Receive Buffer*/
	protected ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER);
	
	/**Last read time*/
	protected long lastReadTime;
	
	/**Send continuation*/
	protected SendContinuation sendContinuation;
	
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
	
	/**
	 * Updating last read time. Invoke after read.
	 */
	protected void updateTimeout() {
		lastReadTime = System.currentTimeMillis();
	}
	
	/**
	 * Returning last read time
	 * @return Read time
	 */
	public long getLastReadTime() {
		return lastReadTime;
	}
	
	/**
	 * Queuing buffer to process it later when write key will ready.
	 * @param buffer Byte buffer
	 */
	protected void queueBuffer(byte[] buffer) {
		writeBuffer.put(buffer);
		writeBuffer.flip();
		
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

	/**
	 * Invoked by NIO server and processing buffer when write key is ready. 
	 * @return true if buffer is empty.
	 * @throws IOException when I/O error occurs.
	 */
	public boolean processQueue() throws IOException {
		processSend(writeBuffer);
		
		if (writeBuffer.remaining() == 0) {
			writeBuffer.clear();
			return true;
		}
		else return false;
	}
	
	/**
	 * Getting selection key
	 * @return SelectionKey
	 */
	public abstract SelectionKey getKey();
	/**
	 * Sending buffer when write key is ready, invoked by {@link processQueue}
	 * @param buffer Buffer to process
	 * @throws IOException
	 */
	public abstract void processSend(ByteBuffer buffer) throws IOException;

}
