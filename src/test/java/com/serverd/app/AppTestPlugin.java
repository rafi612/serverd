package com.serverd.app;

import com.serverd.plugin.Plugin;
import com.serverd.plugin.ServerdPlugin;
import com.serverd.plugin.Plugin.Info;

public class AppTestPlugin implements ServerdPlugin {
	public AppTestPlugin() {}
	@Override
	public void metadata(Info info) {
		info.name = "AppTestPlugin";
	}

	@Override
	public String init(Plugin plugin) {
		if (System.getProperty("running.app") != null) {
			plugin.trace("Plugin runned as app");
			System.setProperty("after.run", "true");	
		}
		return INIT_SUCCESS;
	}

	@Override
	public void work(Plugin plugin) {}

	@Override
	public void stop(Plugin plugin) {}	
}