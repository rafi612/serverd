package com.serverd.plugin;

/**
 * Main plugin interface.
 */
public interface ServerdPlugin {
	/** Plugin init successfully code */
	String INIT_SUCCESS = "";
	
	/**
	 * Executing before initializing plugin, use to set plugin name, author etc.
	 * @param info Info instance
	 */
	void metadata(Plugin.Info info);
	/**
	 * Initializing plugin.
	 * @param plugin Plugin instance
	 * @return When return {@link ServerdPlugin#INIT_SUCCESS} then plugin is successfully loaded, when return String, plugin throw error with message.
	 */
	String init(Plugin plugin);
	/**
	 * Main function of plugin.
	 * @param plugin Plugin instance
	 */
	void work(Plugin plugin);
	/**
	 * Executing on plugin end of work.
	 * @param plugin Plugin instance
	 */
	void stop(Plugin plugin);
}
