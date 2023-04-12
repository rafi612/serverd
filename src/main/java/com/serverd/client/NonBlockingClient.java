package com.serverd.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.LinkedList;

public abstract class NonBlockingClient extends Client 
{
	/** Selector */
	protected Selector selector;
	
	private LinkedList<ByteBuffer> queue = new LinkedList<>();
	
	/**
	 * NonBlockingClient constructor
	 * @param id client ID
	 * @param selector client Selector object
	 */
	public NonBlockingClient(int id,Selector selector) {
		super(id);
		this.selector = selector;
	}
	
	protected void queueBuffer(ByteBuffer buffer) {
		queue.add(buffer);
	}
	
	public ByteBuffer getFromQueue() {
		return queue.poll();
	}
	
	public boolean isQueueEmpty() {
		return queue.isEmpty();
	}
	
	public boolean processQueue() throws IOException {
    	while (!isQueueEmpty()) {
    		ByteBuffer buffer = queue.element();
    		
    		if (buffer.position() >= buffer.limit())
    		{
    			queue.poll();
    			continue;
    		}
    		
    		processSend(buffer);
    	}
    	return true;
	}
	
	public abstract long processSend(ByteBuffer buffer) throws IOException;

}
