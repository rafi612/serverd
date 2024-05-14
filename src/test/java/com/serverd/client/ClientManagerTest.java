package com.serverd.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;

import com.serverd.app.ServerdApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.ServerdPlugin;
import com.serverd.plugin.listener.ConnectListener;
import com.serverd.plugin.Plugin.Info;

class ClientManagerTest {

	ServerdApplication app;

	ClientManager clientManager;

	PluginManager pluginManager;

	@BeforeEach
	void setUp() {
		app = new ServerdApplication();
		clientManager = app.getClientManager();
		pluginManager = app.getPluginManager();
	}

	@AfterEach
	void tearDown() {
		for (Client client : clientManager.clients.values())
			client.closeClient();
		clientManager.clients.clear();
	}
	
	@Test
	void delete_Test() {
		int count = 10;
		for (int i = 0;i < count;i++)
			clientManager.addClient(new Client(i,clientManager));

		clientManager.delete(5);
		
		assertEquals(count - 1,clientManager.clients.size());
	}

	@Test
	void delete_IsDisconnectEventExecuting_Test() {
		AtomicBoolean disconnectExecuted = new AtomicBoolean(false);
		
		Plugin plugin = new Plugin("test",pluginManager,new ServerdPlugin() {
			@Override
			public String init(Plugin plugin) {
				plugin.addConnectListener(new ConnectListener() {
					@Override
					public void onDisconnect(Plugin plugin, Client client) {
						disconnectExecuted.set(true);
					}
					
					@Override
					public void onConnect(Plugin plugin, Client client){}
				});
				return INIT_SUCCESS;
			}
			
			@Override
			public void work(Plugin plugin) {}
			
			@Override
			public void stop(Plugin plugin) {}
			
			@Override
			public void metadata(Info info) {}
		});
		plugin.start();
		pluginManager.addPlugin(plugin);
		
		Client client = new Client(0,clientManager);
		clientManager.addClient(client);

		clientManager.delete(client.getId());
		
		assertTrue(disconnectExecuted.get());
	}
	
	@Test
	void delete_ClientsSizeEqualsZero_Test() {
		clientManager.delete(0);
		
		assertEquals(clientManager.getClientConnectedAmount(), 0);
	}
	
	@Test
	void shutdown_StopClients_Test() {
		class TestClient extends Client {
			public TestClient(int id) { super(id,clientManager); }

			public void run() { connected = true; }
		}
		TestClient client = new TestClient(clientManager.getFreeClientID());
		client.run();
		clientManager.addClient(client);

		clientManager.shutdown();
		
		assertFalse(client.isConnected());
	}
	
	@Test
	void getAllClients_Test() {
		int count = 10;
		for (int i = 0;i < count;i++)
			clientManager.addClient(new Client(i,clientManager));
		
		Client[] clients = clientManager.getAllClients();
		assertEquals(clients.length, count);
		
		for (int i = 0;i < count;i++)
			assertTrue(clientManager.clients.containsValue(clients[i]));
	}
	
	@Test
	void getFreeClientID_Test() {
		for (int i = 0;i < 10;i++) {
			clientManager.addClient(new Client(clientManager.getFreeClientID(),clientManager));
		}
		
		assertEquals(clientManager.getFreeClientID(), 10);

		clientManager.delete(5);
		
		assertEquals(clientManager.getFreeClientID(), 5);
	}
}
