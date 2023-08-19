package com.serverd.server;

import java.io.IOException;

import com.serverd.config.Config;
import com.serverd.log.Log;

/**
 * Server class
 */
public abstract class Server {
	
	/** Logger */
	protected Log log;
	
	/** Is enabled*/
	protected boolean isEnabled = true;
	/** Is runned*/
	protected boolean isRunned;

	/** IP */
	protected String ip;
	/** Port */
	protected int port;
	
	private String name;
	
	/** Config */
	protected Config config;

	/**
	 * Server class constructor
	 * @param name Server name
	 * @param ip Server IP
	 * @param port Server port
	 * @param config Global config
	 */
	public Server(String name,String ip,int port,Config config) {
		log = new Log(name);
		
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.config = config;
	}
	
	/**
	 * Return true if server is enabled
	 * @return true if server is enabled
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Enabling server
	 */
	public void enable() {
		this.isEnabled = true;
	}
	
	/**
	 * Disabling server
	 */
	public void disable() {
		this.isEnabled = false;
	}
	
	/**
	 * Returns server name
	 * @return server name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns true if server is runned
	 * @return true if server is runned
	 */
	public boolean isRunned() {
		return isRunned;
	}
	
	/**
	 * Starting server
	 * @throws IOException if server throws I/O error
	 */
	public abstract void start() throws IOException;
	/**
	 * Stopping server
	 * @throws IOException if server throws I/O error
	 */
	public abstract void stop() throws IOException;
}