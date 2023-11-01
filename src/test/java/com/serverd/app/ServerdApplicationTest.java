package com.serverd.app;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.serverd.client.ClientManager;
import com.serverd.plugin.PluginManager;

class ServerdApplicationTest {

	ClientManager clientManager;

	@BeforeEach
	void setUp() {
		clientManager = new ClientManager();
	}
	
	@AfterEach
	void tearDown() throws Exception {
		PluginManager.unloadAllPlugins();
	}

	@Test
	void app_Test() {
		ServerdApplication app = new ServerdApplication();
		app.run();
	}
	
	@Test
	void run_Test() {
		System.setProperty("running.app", "true");
		ServerdApplication.run(AppTestPlugin.class);
		clientManager.shutdown();
		assertEquals("true",System.getProperty("after.run"));
	}

}