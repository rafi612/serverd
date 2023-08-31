package com.serverd.plugin;

/**
 * Plugin load exception.
 */
public class PluginLoadException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private String pluginName;

	/**
	 * PluginLoadException constructor.
	 * @param pluginName Plugin name
	 * @param message Error message
	 */
	public PluginLoadException(String pluginName,String message) {
		super(message);
		this.pluginName = pluginName;
	}

	/**
	 * PluginLoadException constructor.
	 * @param pluginName Plugin name
	 * @param message Error message
	 * @param cause Throwable cause instance
	 */
	public PluginLoadException(String pluginName,String message, Throwable cause) {
		super(message, cause);
		this.pluginName = pluginName;
	}
	
	/**
	 * @return plugin name.
	 */
	public String getPluginName() {
		return pluginName;
	}
}
