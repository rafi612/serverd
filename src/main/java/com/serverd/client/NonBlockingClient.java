package com.serverd.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;

public abstract class NonBlockingClient extends Client 
{
	/** Selector */
	protected Selector selector;
	
	protected ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER);
	protected ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER);
	
	/**
	 * NonBlockingClient constructor
	 * @param id client ID
	 * @param selector client Selector object
	 */
	public NonBlockingClient(int id,Selector selector) {
		super(id);
		this.selector = selector;
	}
	
	protected void queueBuffer(byte[] buf) {
		writeBuffer.put(buf);
		writeBuffer.flip();
	}
	
	public boolean processQueue() throws IOException {
		
		processSend(writeBuffer);
		
		if (writeBuffer.remaining() == 0)
		{
			writeBuffer.clear();
			return true;
		}
		else return false;
	}
	
	public abstract long processSend(ByteBuffer buffer) throws IOException;

}
