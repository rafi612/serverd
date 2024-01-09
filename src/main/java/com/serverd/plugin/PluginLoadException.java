package com.serverd.plugin;

/**
 * Exception thrown when plugin load failed.
 */
public class PluginLoadException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private final String pluginName;

	/**
	 * PluginLoadException constructor.
	 * @param pluginName Plugin name.
	 * @param message Error message.
	 */
	public PluginLoadException(String pluginName,String message) {
		super(message);
		this.pluginName = pluginName;
	}

	/**
	 * PluginLoadException constructor.
	 * @param pluginName Plugin name.
	 * @param message Error message.
	 * @param cause Throwable cause instance.
	 */
	public PluginLoadException(String pluginName,String message, Throwable cause) {
		super(message, cause);
		this.pluginName = pluginName;
	}
	
	/**
	 * Returns plugin name.
	 */
	public String getPluginName() {
		return pluginName;
	}
}
