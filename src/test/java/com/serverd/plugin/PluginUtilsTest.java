package com.serverd.plugin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.serverd.plugin.Plugin.Info;

class PluginUtilsTest {	
	
	@AfterEach
	void tearDown() throws Exception {
		PluginManager.unloadAllPlugins();
	}
	
	@Test
	void loadPluginFromClassName_Test() {
		assertDoesNotThrow(() -> PluginUtils.loadPluginFromClassName(PluginUtilsTestPlugin.class.getName()));
	}
}

class PluginUtilsTestPlugin implements ServerdPlugin {
	public PluginUtilsTestPlugin() {}
	@Override
	public void metadata(Info info) {}

	@Override
	public String init(Plugin plugin) {
		return INIT_SUCCESS;
	}

	@Override
	public void work(Plugin plugin) {
	}

	@Override
	public void stop(Plugin plugin) {}	
}
