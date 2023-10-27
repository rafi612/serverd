package com.serverd.server;

import com.serverd.config.Config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServerManagerTest {
	
	static class TestServer extends Server {
		public boolean isStarted,isStopped;
		
		public TestServer() { super("Test Server",null,0,null,null); }

		@Override
		public void start() { isStarted = true; }

		@Override
		public void stop() { isStopped = true; }
	}
	
	@BeforeEach
	void setUp() {
		ServerManager.removeAllServers();
	}

	@Test
	void addServer_Test() {
		TestServer server = new TestServer();
		ServerManager.addServer(server);
		
		assertEquals(1,ServerManager.getServers().length);
	}
	
	@Test
	void addDefaultServers_Test() {
		ServerManager.addDefaultServers(null,new Config());
		
		assertEquals(2,ServerManager.getServers().length);
	}
	
	@Test
	void init_Test() {
		int number = 10;
		for (int i = 0;i < number;i++)
			ServerManager.addServer(new TestServer());
		
		ServerManager.init();
		
		Server[] servers = ServerManager.getServers();
		for (Server server : servers)
			assertTrue(server.isEnabled);
	}
	
	@Test
	void shutdown_Test() {
		int number = 10;
		for (int i = 0;i < number;i++)
			ServerManager.addServer(new TestServer());
		
		ServerManager.shutdown();
		
		Server[] servers = ServerManager.getServers();
		for (Server server : servers)
			assertAll(
				() -> assertTrue(((TestServer)server).isStopped),
				() -> assertFalse(server.isRunned)
 			);
	}

}
