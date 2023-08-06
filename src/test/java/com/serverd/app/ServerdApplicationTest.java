package com.serverd.app;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.serverd.client.ClientManager;
import com.serverd.plugin.PluginManager;

class ServerdApplicationTest {
	
	@AfterEach
	void tearDown() throws Exception {
		PluginManager.unloadAllPlugins();
	}
	
	@Test
	void run_Test() {
		System.setProperty("running.app", "true");
		ServerdApplication.run(AppTestPlugin.class);
		ClientManager.shutdown();
		assertEquals("true",System.getProperty("after.run"));
	}

}