package com.serverd.client;

import java.io.IOException;
import java.util.HashMap;

import com.serverd.log.Log;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.listener.ConnectListener;

/**
 * Client Manager
 */
public class ClientManager {
	/** Client's hashmap*/
	public static HashMap<Integer,Client> clients = new HashMap<>();
	
	static boolean tcpRunned = false,udpRunned = false;
	
	private static Log log = new Log("ServerD");
	
	/**
	 * Deleting client
	 * @param clientid Client ID to remove
	 */
	public static synchronized void delete(int clientid) {	
		if (clients.size() == 0)
			return;
		
		Client client = getClient(clientid);
		
		if (client.isJoined())
			client.unjoin();
		
		//plugin connect listener
		for (Plugin p : PluginManager.plugins)
			for (ConnectListener cl : p.connectListeners) {
				try {
					cl.onDisconnect(p,client);
				} catch (IOException e) {
					log.error("Error in Disconnect Listener: " + e.getMessage());
				}
			}
			
		client.closeClient();

		clients.remove(clientid);
			
		log.info("Client " + clientid + " has been closed");
	}
	
	/**
	 * Shutting down server
	 */
	public static void shutdown() {
		log.info("Closing clients...");
		for (Client client : clients.values())
			client.closeClient();
		
		log.info("Stopping plugins...");
		for (Plugin plugin : PluginManager.plugins)
			plugin.stop();
	}
	
	/**
	 * Searching first free client ID
	 * @return first free ID
	 */
	public static int getFreeClientID() {
		int i = 0;
		while (clients.containsKey(i))
			i++;
		return i;
	}
	
	/**
	 * Configures client and executing connect listener.
	 * Can be used in plugins on adding custom protocols.
	 * @param client {@link Client} instance
	 * @throws IOException when {@link ConnectListener} throws error
	 */
	public static void setupClient(Client client) throws IOException {		
		//plugin connect listener
		for (Plugin p : PluginManager.plugins)
			for (ConnectListener cl : p.connectListeners)
				cl.onConnect(p,client);
	}
	
	/**
	 * Adding client
	 * @param client Client object
	 */
	public static void addClient(Client client) {
		clients.put(client.getID(),client);
	}
	
	/**
	 * Returning all clients.
	 * @return Array of clients
	 */
	public static Client[] getAllClients() {
		return clients.values().toArray(Client[]::new);
	}
	
	/**
	 * Returning clients amount
	 * @return clients amount number
	 */
	public static int getClientConnectedAmount() {
		return clients.size();
	}
	
	/**
	 * Returns client instance by ID
	 * @param id Client ID
	 * @return Client instance
	 */
	public static Client getClient(int id) {
		return clients.get(id);
	}
}