package com.serverd.server;

import com.serverd.app.ServerdApplication;
import com.serverd.config.Config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServerManagerTest {

	ServerdApplication app;
	ServerManager serverManager;
	
	static class TestServer extends Server {
		public boolean isStarted,isStopped;
		
		public TestServer() { super("Test Server",null,0,null); }

		@Override
		public void start() { isStarted = true; }

		@Override
		public void stop() { isStopped = true; }
	}
	
	@BeforeEach
	void setUp() {
		app = new ServerdApplication();
		serverManager = app.getServerManager();
		serverManager.removeAllServers();
	}

	@Test
	void addServer_Test() {
		TestServer server = new TestServer();
		serverManager.addServer(server);
		
		assertEquals(1,serverManager.getServers().length);
	}
	
	@Test
	void addDefaultServers_Test() {
		serverManager.addDefaultServers(null,new Config());
		
		assertEquals(2,serverManager.getServers().length);
	}
	
	@Test
	void init_Test() {
		int number = 10;
		for (int i = 0;i < number;i++)
			serverManager.addServer(new TestServer());

		serverManager.init();
		
		Server[] servers = serverManager.getServers();
		for (Server server : servers)
			assertTrue(server.isEnabled);
	}
	
	@Test
	void shutdown_Test() {
		int number = 10;
		for (int i = 0;i < number;i++)
			serverManager.addServer(new TestServer());

		serverManager.shutdown();
		
		Server[] servers = serverManager.getServers();
		for (Server server : servers)
			assertAll(
				() -> assertTrue(((TestServer)server).isStopped),
				() -> assertFalse(server.isRunning)
 			);
	}

}
