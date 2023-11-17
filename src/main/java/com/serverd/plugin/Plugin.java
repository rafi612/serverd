package com.serverd.plugin;

import com.serverd.plugin.listener.ConnectListener;
import com.serverd.plugin.listener.ExecutionController;
import com.serverd.server.Server;
import com.serverd.server.ServerManager;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import com.serverd.command.Command;
import com.serverd.log.Log;

/**
 * Plugin instance class.
 */
public class Plugin {
	private final Info info = new Info();
	
	public ArrayList<ConnectListener> connectListeners = new ArrayList<>();
	public ArrayList<ExecutionController> executionControllers = new ArrayList<>();
	
	public ArrayList<Command> commands = new ArrayList<>();
	public ArrayList<Server> servers = new ArrayList<>();
	
	private final ServerdPlugin instance;

	private boolean isRunned = false;
	
	private boolean isApp = false;
	
	private Log log;
	
	public String filename;
	
	/**
	 * Plugin class constructor.
	 * @param filename Filename of plugin file
	 * @param instance Instance of loaded plugin
	 */
	public Plugin(String filename,ServerdPlugin instance) {
		this.filename = filename;
		this.instance = instance;
	}
	
	/**
	 * Start Plugin.
	 * @return true if plugin load successfully
	 */
	public boolean start() {
		instance.metadata(info);
		
		log = new Log(info.name);
		
		String errorMessage = instance.init(this);
		
		if (errorMessage != null && !errorMessage.isEmpty()) {
			error("Plugin init failed: " + errorMessage);
			return false;
		}

		Thread thread = new Thread(() -> instance.work(this));
		thread.start();
		
		isRunned = true;
		
		return true;
	}
	
	/**
	 * Stop plugin.
	 */
	public void stop() {
		isRunned = false;
		
		instance.stop(this);
		
		//clear interfaces
		commands.clear();
		connectListeners.clear();
		executionControllers.clear();
		
		for (Server server : servers)
			ServerManager.stopServer(server);

		servers.clear();
	}
	
	/**
	 * Print plugin info message.
	 * @param message Message
	 */
	public void info(String message) {
		log.info(message);
	}
	
	/**
	 * Print plugin warning message.
	 * @param message Message
	 */
	public void warn(String message) {
		log.warn(message);
	}
	
	/**
	 * Print plugin error message.
	 * @param message Message
	 */
	public void error(String message) {
		log.error(message);
	}
	
	/**
	 * Print plugin debug message.
	 * @param message Message
	 */
	public void debug(String message) {
		log.debug(message);
	}
	
	/**
	 * Print plugin trace message.
	 * @param message Message
	 */
	public void trace(String message) {
		log.trace(message);
	}
	
	/**
	 * Returning instance of plugin interface, can be used to access variables in plugin main object.
	 * @param <T> Target type
	 * @return instance of plugin interface.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getInstance() {
		return (T) instance;
	}
	
	/**
	 * Loading resource from classloader.
	 * @param path Path to resource
	 * @return {@link InputStream} to resource
	 */
	public InputStream loadResource(String path) {
		return instance.getClass().getResourceAsStream(path);
	}
	
	/**
	 * @return Plugin info object.
	 */
	public Info getInfo() {
		return info;
	}
	
	/**
	 * @return true if plugin is runned.
	 */
	public boolean isRunned() {
		return isRunned;
	}

	/**
	 * Loading plugin workspace folder, if not exists, creating it.
	 * @return File object
	 */
	public File loadWorkspace() {
		File file = isApp && PluginManager.pluginAppDataDir != null ? PluginManager.pluginAppDataDir : new File(PluginManager.pluginDataDir,info.name);
		if (!file.exists())
			file.mkdir();
		
		return file;
	}
	
	/**
	 * Marking plugin as application plugin.
	 */
	public void markAsApp() {
		isApp = true;
	}
	
	/**
	 * Adding ConnectListener.
	 * @param listener Listener instance
	 */
	public void addConnectListener(ConnectListener listener) {
		connectListeners.add(listener);
	}
	
	/**
	 * Removing ConnectListener.
	 * @param listener Listener instance
	 */
	public void removeConnectListener(ConnectListener listener){
		connectListeners.remove(listener);
	}
	
	/**
	 * Adding ExecutionController.
	 * @param listener Listener instance
	 */
	public void addExecutionController(ExecutionController listener) {
		executionControllers.add(listener);
	}
	
	/**
	 * Removing ExecutionController.
	 * @param listener Listener instance
	 */
	public void removeExecutionController(ExecutionController listener) {
		executionControllers.remove(listener);
	}
	
	
	/**
	 * Adding command to plugin commands list.
	 * @param command Command instance
	 */
	public void addCommand(Command command) {
		commands.add(command);
	}
	
	/**
	 * Removing command from plugin commands list.
	 * @param command Command instance
	 */
	public void removeCommand(Command command) {
		commands.remove(command);
	}
	
	
	/**
	 * Adding server to plugin servers list.
	 * @param server Server instance
	 */
	public void addServer(Server server) {
		ServerManager.loadServer(server);
		servers.add(server);
	}
	
	/**
	 * Removing Server from plugin servers list.
	 * @param server Server instance
	 */
	public void removeServer(Server server) {
		servers.remove(server);
	}

	/**
	 * Plugin information class.
	 */
	public static class Info {
		/** Info fields*/
		public String name,author,description,version;

		public String toString() {
			return "Name: " + name + "\nAuthor: " + author + "\nDescription: " + description + "\nVersion: " + version;
		}
	}
}