package com.serverd.plugin;

import com.serverd.plugin.command.Command;
import com.serverd.plugin.listener.ConnectListener;
import com.serverd.plugin.listener.ExecutionController;
import com.serverd.plugin.listener.UpdateIDListener;

import java.io.File;
import java.util.ArrayList;

import com.serverd.log.Log;

/**
 * Plugin instance class
 */
public class Plugin
{
	public Info info = new Info();
	
	public ArrayList<ConnectListener> connectlisteners = new ArrayList<ConnectListener>();
	public ArrayList<UpdateIDListener> updateidlisteners = new ArrayList<UpdateIDListener>();
	public ArrayList<Command> commands = new ArrayList<Command>();
	public ArrayList<ExecutionController> executioncontrollers = new ArrayList<ExecutionController>();
	
	public ServerdPlugin instance;
	private Thread thread;
	
	public boolean isRunned = false;
	
	private Log log;
	
	public File file;
	
	/**
	 * Plugin class constructor
	 * @param file File to plugin
	 * @param instance Instance of loaded plugin
	 */
	public Plugin(File file,ServerdPlugin instance)
	{
		this.file = file;
		this.instance = instance;
	}
	
	/**
	 * Start Plugin
	 * @return Plugin error code
	 */
	public int start()
	{
		instance.metadata(info);
		
		log = new Log(info.name);
		
		String errormessage = instance.init(this);
		
		if (errormessage != null)
			if (!errormessage.equals(""))
		{
			info("Plugin init failed: " + errormessage);
			return 1;
		}
		
		thread = new Thread(() -> instance.work(this));
		thread.start();
		
		isRunned = true;
		
		return 0;
	}
	
	
	/**
	 * Stop plugin
	 */
	public void stop()
	{
		isRunned = false;
		
		instance.stop(this);
		
		//clear interfaces
		commands.clear();
		connectlisteners.clear();
		updateidlisteners.clear();
		executioncontrollers.clear();
		
		PluginManager.plugins_loaded--;
	}
	
	
	
	/**
	 * Print plugin info message
	 * @param message Message
	 */
	public void info(String message)
	{
		log.info(message);
	}
	
	/**
	 * Print plugin warning
	 * @param message Message
	 */
	public void warn(String message)
	{
		log.warn(message);
	}
	
	/**
	 * Print plugin error
	 * @param message Message
	 */
	public void error(String message)
	{
		log.error(message);
	}
	
	/**
	 * Returning instance of plugin object, can be use to access 
	 * static variables in plugin main object, before use must be casted to plugin main class
	 * @return instance of plugin object
	 */
	public ServerdPlugin getInstance()
	{
		return instance;
	}
	
	/**
	 * Loading plugin workspace folder, if not exists, creating it
	 * @return File object
	 */
	public File loadWorkspace()
	{
		File f = new File(PluginManager.plugindatadir,info.name);
		if (!f.exists())
		{
			f.mkdir();
			return f;
		}
		else return f;
	}
	
	/**
	 * Adding ConnectListener
	 * @param listener Listener instance
	 */
	public void addConnectListener(ConnectListener listener)
	{
		connectlisteners.add(listener);
	}
	
	/**
	 * Removing ConnectListener
	 * @param listener Listener instance
	 */
	public void removeConnectListener(ConnectListener listener)
	{
		connectlisteners.remove(listener);
	}
	
	/**
	 * Adding command
	 * @param command Command instance
	 */
	public void addCommand(Command command)
	{
		commands.add(command);
	}
	
	/**
	 * Removing command
	 * @param command Command instance
	 */
	public void removeCommand(Command command)
	{
		commands.remove(command);
	}
	
	/**
	 * Adding UpdateIDListener
	 * @param listener Listener instance
	 */
	public void addUpdateIDListener(UpdateIDListener listener)
	{
		updateidlisteners.add(listener);
	}
	/**
	 * Removing UpdateIDListener
	 * @param listener Listener instance
	 */
	public void removeUpdateIDListener(UpdateIDListener listener)
	{
		updateidlisteners.remove(listener);
	}
	
	/**
	 * Adding ExecutionController
	 * @param listener Listener instance
	 */
	public void addExecutionController(ExecutionController listener)
	{
		executioncontrollers.add(listener);
	}
	/**
	 * Removing ExecutionController
	 * @param listener Listener instance
	 */
	public void removeExecutionController(ExecutionController listener)
	{
		executioncontrollers.remove(listener);
	}

	/**
	 * Plugin information class
	 */
	public class Info
	{
		public String name,author,decription,version;
		public String toString()
		{
			return "Name: " + name + "\nAuthor: " + author + "\nDescription: " + decription + "\nVersion: " + version;
		}
	}
}
