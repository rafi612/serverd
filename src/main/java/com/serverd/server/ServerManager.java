package com.serverd.server;

import java.io.IOException;
import java.util.ArrayList;

import com.serverd.app.ServerdApplication;
import com.serverd.client.ClientManager;
import com.serverd.client.processor.Processor;
import com.serverd.client.processor.ProcessorFactory;
import com.serverd.command.CommandProcessor;
import com.serverd.config.Config;
import com.serverd.log.Log;

/**
 * Server manager class.
 * It maintains a list of servers, handles server initialization, loading, and shutdown procedures.
 * Additionally, it provides methods for adding, removing servers, setting default processors, and obtaining default TCP and UDP servers.
 */
public class ServerManager {
	private static final ArrayList<Server> servers = new ArrayList<>();
	private static final Log log = Log.get(ServerManager.class);
	private TCPServer tcpServer;
	private UDPServer udpServer;
	private ProcessorFactory defaultProcessorFactory = (CommandProcessor::new);
	private final ServerdApplication app;


	public ServerManager(ServerdApplication app) {
		this.app = app;
	}
	
	/**
	 * Returning array of added server.
	 * @return array of added server.
	 */
	public Server[] getServers() {
		return servers.toArray(Server[]::new);
	}
	
	/**
	 * Add default servers to server list.
	 * @param config Default server config.
	 */
	public void addDefaultServers(ClientManager clientManager,Config config) {
		addServer(tcpServer = new TCPServer(config.ip, config.tcpPort,clientManager, config));
		addServer(udpServer = new UDPServer(config.ip, config.udpPort,clientManager, config));
	}
	
	/**
	 * Cleaning all server.
	 */
	protected void removeAllServers() {
		servers.clear();
	}
	
	/**
	 * Initializing Server Manager.
	 */
	public void init() {
		for (Server server : servers)
			loadServer(server);
	}
	
	/**
	 * Loading server.
	 * @param server Server instance.
	 */
	public void loadServer(Server server) {
		if (!server.isEnabled()) {
			log.info(server.getName() + " was disabled");
			return;
		}

		server.setServerManager(this);
		server.setApp(app);
			
		new Thread(() -> {
			try {
				server.isRunning = true;
				log.info("Starting " + server.getName());
				server.start();
			} catch (IOException e) {
				log.error("[" + server.getName() + "] Server error: " + e.getMessage());
			}
		},server.getName()).start();
	}
	
	/**
	 * Shutting down Server Manager.
	 */
	public void shutdown() {
		log.info("Server shutting down...");
		
		for (Server server : servers)
			stopServer(server);
	}
	
	/**
	 * Stopping server.
	 * @param server Server instance.
	 */
	public void stopServer(Server server) {
		try {
			server.isRunning = false;
			server.stop();
		} catch (IOException e) {
			log.error("Shutdown error: " + e.getMessage());
		}
	}
	
	/**
	 * Returns default TCP Server.
	 * @return Default TCP Server.
	 */
	public TCPServer getTcpServer() {
		return tcpServer;
	}

	/**
	 * Returns default UDP Server.
	 * @return Default UDP Server.
	 */
	public UDPServer getUdpServer() {
		return udpServer;
	}


	/**
	 * Adding server.
	 * @param server Server object.
	 */
	public void addServer(Server server) {
		servers.add(server);
	}
	
	/**
	 * Removing server.
	 * @param server Server object.
	 */
	public void removeServer(Server server) {
		servers.remove(server);
	}
	
	/**
	 * Setting default processor factory.
	 * @param factory Factory interface or lambda.
	 * @see Processor
	 */
	public void setDefaultProcessorFactory(ProcessorFactory factory) {
		defaultProcessorFactory = factory;
	}
	
	/**
	 * @return Default processor factory.
	 * @see Processor
	 */
	public ProcessorFactory getDefaultProcessorFactory() {
		return defaultProcessorFactory;
	}
	
	/**
	 * Resetting processor factory to default.
	 */
	public void resetDefaultProcessorFactory() {
		setDefaultProcessorFactory(CommandProcessor::new);
	}
}