package com.serverd.server;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.serverd.client.ClientManager;
import com.serverd.client.SelectableClient;

/**
 * Server class for selectable servers (Using java NIO API).
 */
public abstract class SelectableServer extends Server {
	
	private long lastTimeout = System.currentTimeMillis();

	/**
	 * SelectableServer class constructor.
	 * @param name Server name.
	 * @param ip Server IP.
	 * @param port Server port.
	 * @param clientManager Client manager object.
	 */
	public SelectableServer(String name, String ip, int port, ClientManager clientManager) {
		super(name,ip,port,clientManager);
	}
	
	/**
	 * Selecting Selector and removing clients that have exceeded the timeout.
	 * @param selector Selector object.
	 * @param channel Server channel object.
	 * @param timeout Timeout value.
	 * @throws IOException if selector throw I/O error.
	 */
	protected void selectWithTimeout(Selector selector,SelectableChannel channel,int timeout) throws IOException {
		// Timeout checking and selecting
		selector.select(timeout);
		if (timeout > 0 && System.currentTimeMillis() - lastTimeout >= timeout) {
			for (SelectionKey key : selector.keys()) {
				// Check if channel can be readable
				if (key.channel().equals(channel))
					continue;
				// Check timeout
				SelectableClient client = (SelectableClient) key.attachment();
				if (System.currentTimeMillis() - client.getLastReadTime() >= timeout)
					client.crash(new IOException("Read timed out"));
			}
			lastTimeout = System.currentTimeMillis();
		}
	}

}
