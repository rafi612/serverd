package com.serverd.plugin.listener;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

/**
 * Client connection listener.
 */
public interface ConnectListener {
	/**
	 * Executing when new client connected to server.
	 * @param plugin Plugin instancd
	 * @param client Connected client instance
	 * @throws IOException when client throw {@link IOException}.
	 */
	public void onConnect(Plugin plugin,Client client) throws IOException;
	/**
	 * Executing when client disconnected from server.
	 * @param plugin Plugin instancd
	 * @param client Disconnected client instance
	 * @throws IOException when client throw {@link IOException}.
	 */
	public void onDisconnect(Plugin plugin,Client client) throws IOException;
}
