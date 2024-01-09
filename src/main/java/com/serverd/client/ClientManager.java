package com.serverd.client;

import java.io.IOException;
import java.util.HashMap;

import com.serverd.app.ServerdApplication;
import com.serverd.log.Log;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.listener.ConnectListener;

/**
 * Client manager class.
 * Client manager allows to manage clients connected to server.
 */
public class ClientManager {
	/** Client's hashmap*/
	public HashMap<Integer,Client> clients = new HashMap<>();
	
	private final Log log = Log.get(ClientManager.class);

	private final ServerdApplication app;

	public ClientManager(ServerdApplication app) {
		this.app = app;
	}
	
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
		for (Plugin plugin : app.getPluginManager().getPlugins())
			for (ConnectListener cl : plugin.connectListeners) {
				try {
					cl.onDisconnect(plugin,client);
				} catch (IOException e) {
					log.error("Error in Disconnect Listener: " + e.getMessage());
				}
			}

		client.getProcessor().onClose();
		client.closeClient();
		
		clients.remove(clientId);
	}
	
	/**
	 * Shutting down server.
	 */
	public void shutdown() {
		log.info("Closing clients...");
		for (Client client : clients.values())
			client.closeClient();
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
		client.getProcessor().onOpen();

		//plugin connect listener
		for (Plugin plugin : app.getPluginManager().getPlugins())
			for (ConnectListener cl : plugin.connectListeners)
				cl.onConnect(plugin,client);
	}

	public ServerdApplication getApp() {
		return app;
	}
	
	/**
	 * Adding client.
	 * @param client Client object.
	 */
	public void addClient(Client client) {
		clients.put(client.getID(),client);
	}
	
	/**
	 * Returns array of all clients stored by this client manager.
	 */
	public Client[] getAllClients() {
		return clients.values().toArray(Client[]::new);
	}
	
	/**
	 * Returns clients amount number.
	 */
	public int getClientConnectedAmount() {
		return clients.size();
	}
	
	/**
	 * Returns client instance by ID.
	 * @param id Client ID.
	 */
	public Client getClient(int id) {
		return clients.get(id);
	}
}