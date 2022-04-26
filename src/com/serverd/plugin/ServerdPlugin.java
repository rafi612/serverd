package com.serverd.plugin;

/**
 * Main plugin interface
 */
public interface ServerdPlugin
{
	/**
	 * Executing before initializing plugin, use to set plugin name, author etc.
	 * @param info Info instance
	 */
	public void metadata(Plugin.Info info);
	/**
	 * Initializing plugin
	 * @param plugin Plugin instance
	 * @return When method return "null" or "" then plugin is succesfully loaded
	 */
	public String init(Plugin plugin);
	/**
	 * Main function of plugin
	 * @param plugin Plugin instance
	 */
	public void work(Plugin plugin);
	/**
	 * Executing on plugin end of work
	 * @param plugin Plugin instance
	 */
	public void stop(Plugin plugin);
}
