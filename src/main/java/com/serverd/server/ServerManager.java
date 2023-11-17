package com.serverd.server;

import java.io.IOException;
import java.util.ArrayList;

import com.serverd.client.processor.Processor;
import com.serverd.client.processor.ProcessorFactory;
import com.serverd.command.CommandProcessor;
import com.serverd.config.Config;
import com.serverd.log.Log;

/**
 * Server manager class.
 */
public class ServerManager {
	
	private static ArrayList<Server> servers = new ArrayList<>();
	
	private static Log log = new Log("ServerD");

	private static TCPServer tcpServer;
	private static UDPServer udpServer;
	
	private static ProcessorFactory defaultProcessorFactory;
	
	/**
	 * Returning array of added server.
	 * @return array of added server.
	 */
	public static Server[] getServers() {
		return servers.toArray(Server[]::new);
	}
	
	/**
	 * Add default servers to server list.
	 * @param config Default server config.
	 */
	public static void addDefaultServers(Config config) {
		addServer(tcpServer = new TCPServer(config.ip, config.tcpPort, config));
		addServer(udpServer = new UDPServer(config.ip, config.udpPort, config));
	}
	
	/**
	 * Cleaning all server.
	 */
	protected static void removeAllServers() {
		servers.clear();
	}
	
	/**
	 * Initalizing Server Manager.
	 */
	public static void init() {
		resetDefaultProcessorFactory();
		
		for (Server server : servers)
			loadServer(server);
	}
	
	/**
	 * Loading server.
	 * @param server Server instance.
	 */
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
	 * Shutting down Server Manager.
	 */
	public static void shutdown() {
		log.info("Server shutting down...");
		
		for (Server server : servers)
			stopServer(server);
	}
	
	/**
	 * Stopping server.
	 * @param server Server instance.
	 */
	public static void stopServer(Server server) {
		try {
			server.isRunned = false;
			server.stop();
		} catch (IOException e) {
			log.error("Shutdown error: " + e.getMessage());
		}
	}
	
	/**
	 * Returns default TCP Server.
	 * @return Default TCP Server.
	 */
	public static TCPServer getTcpServer() {
		return tcpServer;
	}

	/**
	 * Returns default UDP Server.
	 * @return Default UDP Server.
	 */
	public static UDPServer getUdpServer() {
		return udpServer;
	}


	/**
	 * Adding server.
	 * @param server Server object.
	 */
	public static void addServer(Server server) {
		servers.add(server);
	}
	
	/**
	 * Removing server.
	 * @param server Server object.
	 */
	public static void removeServer(Server server) {
		servers.remove(server);
	}
	
	/**
	 * Setting default processor factory.
	 * @param factory Factory interface or lambda.
	 * @see Processor
	 */
	public static void setDefaultProcessorFactory(ProcessorFactory factory) {
		defaultProcessorFactory = factory;
	}
	
	/**
	 * @return Default processor factory.
	 * @see Processor
	 */
	public static ProcessorFactory getDefaultProcessorFactory() {
		return defaultProcessorFactory;
	}
	
	/**
	 * Resetting processor factory to default.
	 */
	public static void resetDefaultProcessorFactory() {
		setDefaultProcessorFactory(CommandProcessor::new);
	}
}