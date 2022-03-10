package com.serverd.plugin;

public interface ServerdPlugin
{
	public void metadata(Plugin.Info info);
	public String init(Plugin info);
	public void work(Plugin plugin);
	public void stop();
}
