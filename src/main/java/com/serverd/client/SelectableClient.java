package com.serverd.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Class representing selectable clients.
 * Primary used by NIO selectable clients.
 */
public abstract class SelectableClient extends Client {
	/** Selector */
	protected Selector selector;
	
	/** Write Buffer */
	protected ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER);
	/** Receive Buffer */
	protected ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER);

	private long lastReadTime;
	
	private SendContinuation sendContinuation;
	
	/**
	 * SelectableClient constructor.
	 * @param id client ID.
	 * @param selector Client's Selector object
	 */
	public SelectableClient(int id, ClientManager clientManager,Selector selector) {
		super(id,clientManager);
		this.selector = selector;
		
		updateTimeout();
	}
	
	/**
	 * Updating last read time. This will be invoked after reading data from the socket.
	 */
	protected void updateTimeout() {
		lastReadTime = System.currentTimeMillis();
	}
	
	/**
	 * Returns read time.
	 */
	public long getLastReadTime() {
		return lastReadTime;
	}
	
	/**
	 * Queuing buffer to process it later when write key will ready.
	 * @param buffer Byte buffer array.
	 * @param continuation Send continuation handler
	 */
	protected void queueBuffer(byte[] buffer,SendContinuation continuation) {
		writeBuffer.put(buffer);
		writeBuffer.flip();
		
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
		
		if (writeBuffer.remaining() == 0) {
			// Reset keys
			getKey().interestOps(0);

			sendContinuation.invoke();
			sendContinuation = null;

			// Unlock read (add OP_READ key)
			if (autoRead)
				unlockRead();

			writeBuffer.clear();
			return true;
		}
		else return false;
	}
	
	/**
	 * Returns {@link SelectionKey} for Client.
	 */
	public abstract SelectionKey getKey();
	/**
	 * Sending buffer when write key is ready, invoked by {@link SelectableClient#processQueue}.
	 * @param buffer Buffer to process.
	 * @throws IOException when I/O error occurs.
	 */
	public abstract void processSend(ByteBuffer buffer) throws IOException;

}
