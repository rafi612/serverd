package com.serverd.client;

import java.io.IOException;
import java.util.HashMap;

import com.serverd.log.Log;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.listener.ConnectListener;

/**
 * Client manager class.
 * Client manager allows to manage clients connected to server.
 */
public class ClientManager {
	/** Client's hashmap*/
	public HashMap<Integer,Client> clients = new HashMap<>();
	
	private Log log = new Log("ServerD");
	
	/**
	 * Deleting client.
	 * @param clientId Client ID to remove.
	 */
	public synchronized void delete(int clientId) {
		if (clients.isEmpty())
			return;
		
		Client client = getClient(clientId);

		if (client == null)
			return;
		
		if (client.isJoined())
			client.unjoin();
		
		//plugin connect listener
		for (Plugin plugin : PluginManager.getPlugins())
			for (ConnectListener cl : plugin.connectListeners) {
				try {
					cl.onDisconnect(plugin,client);
				} catch (IOException e) {
					log.error("Error in Disconnect Listener: " + e.getMessage());
				}
			}
			
		client.closeClient();
		client.getProcessor().printDeleteMessage(client, log);
		
		clients.remove(clientId);
	}
	
	/**
	 * Shutting down server.
	 */
	public void shutdown() {
		log.info("Closing clients...");
		for (Client client : clients.values())
			client.closeClient();
		
		log.info("Stopping plugins...");
		for (Plugin plugin : PluginManager.getPlugins())
			plugin.stop();
	}
	
	/**
	 * Searching first free client ID.
	 * @return first free ID.
	 */
	public int getFreeClientID() {
		int i = 0;
		while (clients.containsKey(i))
			i++;
		return i;
	}
	
	/**
	 * Configures client and executing connect listener.
	 * Can be used in plugins on adding custom protocols.
	 * @param client {@link Client} instance
	 * @throws IOException when {@link ConnectListener} throws error.
	 */
	public void setupClient(Client client) throws IOException {
		//plugin connect listener
		for (Plugin plugin : PluginManager.getPlugins())
			for (ConnectListener cl : plugin.connectListeners)
				cl.onConnect(plugin,client);
	}
	
	/**
	 * Adding client.
	 * @param client Client object.
	 */
	public void addClient(Client client) {
		clients.put(client.getID(),client);
	}
	
	/**
	 * Returning all clients.
	 * @return Array of clients.
	 */
	public Client[] getAllClients() {
		return clients.values().toArray(Client[]::new);
	}
	
	/**
	 * @return clients amount number.
	 */
	public int getClientConnectedAmount() {
		return clients.size();
	}
	
	/**
	 * Returns client instance by ID.
	 * @param id Client ID.
	 * @return Client instance.
	 */
	public Client getClient(int id) {
		return clients.get(id);
	}
}