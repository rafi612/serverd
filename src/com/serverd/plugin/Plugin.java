package com.serverd.plugin;

import com.serverd.plugin.command.Command;
import com.serverd.plugin.listener.ConnectListener;
import com.serverd.plugin.listener.UpdateIDListener;

import java.io.File;
import java.util.ArrayList;

import com.serverd.log.Log;

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
	
	public Plugin(File file,ServerdPlugin instance)
	{
		this.file = file;
		this.instance = instance;
	}
	
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
	
	public void Log(String text)
	{
		Log.log(info.name,text);
	}
	
	public void stop()
	{
		isRunned = false;
		
		instance.stop();
		
		//removing listeners
		connectlisteners.clear();
		
		//removing commands
		commands.clear();
		
		updateidlisteners.clear();
		
		if (thread.isAlive())
			thread.interrupt();
		
		PluginManager.plugins_loaded--;
	}
	
	public void warn(String message)
	{
		Log("WARN: " + message);
	}
	
	public void error(String message)
	{
		Log("ERROR: " + message);
		stop();
	}
	
	public void addConnectListener(ConnectListener c)
	{
		connectlisteners.add(c);
	}
	
	public void removeConnectListener(ConnectListener c)
	{
		connectlisteners.remove(c);
	}
	
	public void addCommand(Command c)
	{
		commands.add(c);
	}
	
	public void removeCommand(Command c)
	{
		commands.remove(c);
	}
	
	public void addUpdateIDListener(UpdateIDListener c)
	{
		updateidlisteners.add(c);
	}
	
	public void removeUpdateIDListener(UpdateIDListener c)
	{
		updateidlisteners.remove(c);
	}

	public class Info
	{
		public String name,author,decription,version;
		public String toString()
		{
			return "Name: " + name + "\nAuthor: " + author + "\nDescription: " + decription + "\nVersion:" + version;
		}
	}
}
