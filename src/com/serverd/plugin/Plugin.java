package com.serverd.plugin;

import com.serverd.plugin.command.Command;
import com.serverd.plugin.listener.ConnectListener;
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
	
	public ServerdPlugin instance;
	Thread thread;
	
	public boolean isRunned = false;
	
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
		String errormessage = instance.init(this);
		
		if (errormessage != null)
			if (!errormessage.equals(""))
		{
			Log("Plugin init failed: " + errormessage);
			return 1;
		}
		
		thread = new Thread(() -> instance.work(this));
		thread.start();
		
		isRunned = true;
		
		return 0;
	}
	
	/**
	 * Print plugin message to stdin
	 * @param message Message
	 */
	public void Log(String message)
	{
		Log.log(info.name,message);
	}
	
	/**
	 * Stop plugin
	 */
	public void stop()
	{
		isRunned = false;
		
		instance.stop(this);
		
		//removing listeners
		connectlisteners.clear();
		
		//removing commands
		commands.clear();
		
		updateidlisteners.clear();
		
		if (thread.isAlive())
			thread.interrupt();
		
		PluginManager.plugins_loaded--;
	}
	
	/**
	 * Print plugin warning
	 * @param message Message
	 */
	public void warn(String message)
	{
		Log("WARN: " + message);
	}
	
	/**
	 * Print plugin error
	 * @param message Message
	 */
	public void error(String message)
	{
		Log("ERROR: " + message);
		stop();
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
	 * Plugin information class
	 */
	public class Info
	{
		public String name,author,decription,version;
		public String toString()
		{
			return "Name: " + name + "\nAuthor: " + author + "\nDescription: " + decription + "\nVersion:" + version;
		}
	}
}
