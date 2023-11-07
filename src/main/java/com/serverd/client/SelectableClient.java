package com.serverd.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Selectable Client class. Used by NIO selectable clients.
 */
public abstract class SelectableClient extends Client {
	/** Selector */
	protected Selector selector;
	
	/**Write Buffer*/
	protected ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER);
	/**Receive Buffer*/
	protected ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER);

	private long lastReadTime;
	
	private SendContinuation sendContinuation;
	
	/**
	 * SelectableClient constructor.
	 * @param id client ID
	 * @param selector client Selector object
	 */
	public SelectableClient(int id,Selector selector) {
		super(id);
		this.selector = selector;
		
		updateTimeout();
	}
	
	/**
	 * Updating last read time. This will be invoked after read.
	 */
	protected void updateTimeout() {
		lastReadTime = System.currentTimeMillis();
	}
	
	/**
	 * @return Read time
	 */
	public long getLastReadTime() {
		return lastReadTime;
	}
	
	/**
	 * Queuing buffer to process it later when write key will ready.
	 * @param buffer Byte buffer
	 * @param continuation Send continuation handler
	 */
	protected void queueBuffer(byte[] buffer,SendContinuation continuation) {
		writeBuffer.put(buffer);
		writeBuffer.flip();
		
		if (isJoined())
			getJoiner().lockRead();
		
		sendContinuation = continuation;
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
		sendContinuation.invoke();
		sendContinuation = null;
		
		if (writeBuffer.remaining() == 0) {
			writeBuffer.clear();
			return true;
		}
		else return false;
	}
	
	/**
	 * @return SelectionKey for Client.
	 */
	public abstract SelectionKey getKey();
	/**
	 * Sending buffer when write key is ready, invoked by {@link SelectableClient#processQueue}.
	 * @param buffer Buffer to process
	 * @throws IOException when I/O error occurs.
	 */
	public abstract void processSend(ByteBuffer buffer) throws IOException;

}
