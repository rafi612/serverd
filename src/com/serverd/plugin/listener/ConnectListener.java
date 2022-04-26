package com.serverd.plugin.listener;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

/**
 * Client connection listener
 */
public interface ConnectListener
{
	/**
	 * Executing when new client connected to server
	 * @param plugin Plugin instancd
	 * @param client Connected client instance
	 */
	public void onConnect(Plugin plugin,Client client);
	/**
	 * Executing when client disconnected from server
	 * @param plugin Plugin instancd
	 * @param client Disconnected client instance
	 */
	public void onDisconnect(Plugin plugin,Client client);
}
