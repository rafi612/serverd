package com.serverd.server;

import java.io.IOException;
import java.util.ArrayList;

import com.serverd.config.Config;
import com.serverd.log.Log;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;

/**
 * Server manager class
 */
public class ServerManager {
	
	private static ArrayList<Server> servers = new ArrayList<>();
	
	private static Log log = new Log("ServerD");
	
	private static boolean inited = false;

	private static TCPServer tcpServer;
	private static UDPServer udpServer;
	
	/**
	 * Returning array of added server
	 * @return array of added server
	 */
	public static Server[] getServers() {
		return servers.toArray(Server[]::new);
	}
	
	/**
	 * Add default servers to server list
	 * @param config Default server config
	 */
	public static void addDefaultServers(Config config) {
		addServer(tcpServer = new TCPServer(config.ip, config.tcpPort, config));
		addServer(udpServer = new UDPServer(config.ip, config.udpPort, config));
	}
	
	/**
	 * Cleaning all server
	 */
	protected static void removeAllServers() {
		servers.clear();
	}
	
	/**
	 * Initalizing Server Manager
	 */
	public static void init() {
		inited = true;
		for (Server server : servers)
			loadServer(server);
		
		//init servers from plugins
		for (Plugin plugin : PluginManager.getPlugins())
			for (Server server : plugin.servers)
				ServerManager.loadServer(server);
	}
	
	public static void loadServer(Server server) {
		if (!server.isEnabled()) {
			log.info(server.getName() + " was disabled");
			return;
		}
			
		new Thread(() -> {
			try {
				server.isRunned = true;
				log.info("Starting " + server.getName());
				server.start();
			} catch (IOException e) {
				log.error("Server error: " + e.getMessage());
			}
		},server.getName()).start();
	}
	
	/**
	 * Shuttting down Server Manager
	 */
	public static void shutdown() {
		log.info("Server shutting down...");
		
		for (Server server : servers)
			stopServer(server);
	}
	
	public static void stopServer(Server server) {
		try {
			server.isRunned = false;
			server.stop();
		} catch (IOException e) {
			log.error("Shutdown error: " + e.getMessage());
		}
	}
	
	/**
	 * Returns default TCP Server
	 * @return Default TCP Server
	 */
	public static TCPServer getTcpServer() {
		return tcpServer;
	}

	/**
	 * Returns default UDP Server
	 * @return Default UDP Server
	 */
	public static UDPServer getUdpServer() {
		return udpServer;
	}


	/**
	 * Adding server 
	 * @param server Server object
	 */
	public static void addServer(Server server) {
		servers.add(server);
	}
	
	/**
	 * Removing server 
	 * @param server Server object
	 */
	public static void removeServer(Server server) {
		servers.remove(server);
	}
	
	/**
	 * Returns if ServerManager is initialized
	 * @return if ServerManager is initialized
	 */
	public static boolean isInitialized() {
		return inited;
	}
}
