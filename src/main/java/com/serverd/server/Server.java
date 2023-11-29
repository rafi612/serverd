package com.serverd.server;

import java.io.IOException;

import com.serverd.app.ServerdApplication;
import com.serverd.client.Client;
import com.serverd.client.ClientManager;
import com.serverd.client.processor.Processor;
import com.serverd.client.processor.ProcessorFactory;
import com.serverd.command.CommandProcessor;
import com.serverd.config.Config;
import com.serverd.log.Log;

/**
 * Server class.
 */
public abstract class Server {
	/** Logger */
	protected Log log;
	/** Is enabled */
	protected boolean isEnabled = true;
	/** Is runned */
	protected boolean isRunned;
	/** IP */
	protected String ip;
	/** Port */
	protected int port;
	private String name;
	/** Config */
	protected Config config;
	
	private ProcessorFactory processorFactory;

	protected ClientManager clientManager;

	public ServerManager getServerManager() {
		return serverManager;
	}

	public void setServerManager(ServerManager serverManager) {
		this.serverManager = serverManager;
	}

	protected ServerManager serverManager;

	protected ServerdApplication app;

	/**
	 * Server class constructor.
	 * @param name Server name
	 * @param ip Server IP
	 * @param port Server port
	 * @param config Global config
	 */
	public Server(String name, String ip, int port,ClientManager clientManager,Config config) {
		log = new Log(name);
		
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.config = config;

		this.clientManager = clientManager;
		
		resetProcessorFactory();
	}
	
	/**
	 * @return true if server is enabled.
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Enabling server.
	 */
	public void enable() {
		this.isEnabled = true;
	}
	
	/**
	 * Disabling server.
	 */
	public void disable() {
		this.isEnabled = false;
	}
	
	/**
	 * @return server name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setting server name.
	 * @param name Server name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return true if server is run.
	 */
	public boolean isRunned() {
		return isRunned;
	}
	
	/**
	 * Setting processor factory.
	 * @param factory Factory interface or lambda.
	 * @see Processor
	 */
	public void setProcessorFactory(ProcessorFactory factory) {
		this.processorFactory = factory;
	}
	
	/**
	 * @return Processor factory of server.
	 * @see Processor
	 */
	public ProcessorFactory getProcessorFactory() {
		return processorFactory == null ? serverManager.getDefaultProcessorFactory() : processorFactory;
	}
	
	/**
	 * Resetting processor factory to default.
	 */
	public void resetProcessorFactory() {
		setProcessorFactory(CommandProcessor::new);
	}
	
	/**
	 * Starting server.
	 * @throws IOException if server throws I/O error.
	 */
	public abstract void start() throws IOException;
	/**
	 * Stopping server.
	 * @throws IOException if server throws I/O error.
	 */
	public abstract void stop() throws IOException;

	/**
	 * @return Server IP Address.
	 */
	public String getIP() {
		return ip;
	}


	/**
	 * Setting server IP.
	 * @param ip Server IP
	 */
	public void setIP(String ip) {
		this.ip = ip;
	}

	/**
	 * @return Server port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Setting server port.
	 * @param port Server port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	protected void setupClientProcessor(Client client, Processor processor) {
		processor.setApp(app);
		client.setProcessor(processor);
	}

	public ServerdApplication getApp() {
		return app;
	}

	public void setApp(ServerdApplication app) {
		this.app = app;
	}
}