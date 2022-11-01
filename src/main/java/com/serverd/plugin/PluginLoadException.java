package com.serverd.plugin;

public class PluginLoadException extends Exception 
{
	private static final long serialVersionUID = 1L;
	
	private String pluginName;

	public PluginLoadException(String pluginName,String message)
	{
		super(message);
		this.pluginName = pluginName;
	}

	public PluginLoadException(String pluginName,String message, Throwable cause) 
	{
		super(message, cause);
		this.pluginName = pluginName;
	}
	
	public String getPluginName()
	{
		return pluginName;
	}

}
