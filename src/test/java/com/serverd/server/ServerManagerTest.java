package com.serverd.server;

import com.serverd.config.Config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ServerManagerTest {
	
	class TestServer extends Server {
		public boolean isStarted,isStopped;
		
		public TestServer() { super("Test Server",null,0,null); }

		@Override
		public void start() throws IOException { isStarted = true; }

		@Override
		public void stop() throws IOException { isStopped = true; }
	}

	@AfterEach
	void tearDown() throws Exception {
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
		ServerManager.addDefaultServers(new Config());
		
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
			assertTrue(((TestServer)server).isEnabled);
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
				() -> assertFalse(((TestServer)server).isRunned)
 			);
	}

}
