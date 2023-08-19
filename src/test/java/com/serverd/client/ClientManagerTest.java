package com.serverd.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.ServerdPlugin;
import com.serverd.plugin.listener.ConnectListener;
import com.serverd.plugin.Plugin.Info;

class ClientManagerTest {	
	@AfterEach
	void tearDown() throws Exception {
		for (Client client : ClientManager.clients.values())
			client.closeClient();
		ClientManager.clients.clear();
	}
	
	@Test
	void delete_Test() {
		int count = 10;
		for (int i = 0;i < count;i++)
			ClientManager.addClient(new Client(i));
		
		ClientManager.delete(5);
		
		assertEquals(ClientManager.clients.size(), 9);
	}
	
	@Test
	void delete_WhenJoinedUnjoin_Test() throws Exception {
		int count = 10;
		for (int i = 0;i < count;i++)
			ClientManager.addClient(new Client(i));
		
		Client client1 = ClientManager.getClient(4);
		Client client2 = ClientManager.getClient(5);
		
		client1.join(client2.getID());
		
		ClientManager.delete(client2.getID());
		
		assertAll(
			() -> assertEquals(9,ClientManager.clients.size()),
			() -> assertFalse(client1.isJoined()),
			() -> assertFalse(client2.isJoined())
		);
	}
	
	@Test
	void delete_IsDisconnectEventExecuting_Test() {
		AtomicBoolean disconnectExecuted = new AtomicBoolean(false);
		
		Plugin plugin = new Plugin("test",new ServerdPlugin() {
			@Override
			public String init(Plugin plugin) 
			{
				plugin.addConnectListener(new ConnectListener() {
					@Override
					public void onDisconnect(Plugin plugin, Client client) throws IOException {
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
		PluginManager.addPlugin(plugin);
		
		Client client = new Client(0);
		ClientManager.addClient(client);
		
		ClientManager.delete(client.getID());
		
		assertTrue(disconnectExecuted.get());
	}
	
	@Test
	void delete_ClientsSizeEqualsZero_Test() {
		ClientManager.delete(0);
		
		assertEquals(ClientManager.getClientConnectedAmount(), 0);
	}
	
	@Test
	void shutdown_StopClients_Test() {
		class TestClient extends Client {
			public TestClient(int id) { super(id); }

			public void run() { connected = true; }
		}
		TestClient client = new TestClient(ClientManager.getFreeClientID());
		client.run();
		ClientManager.addClient(client);
		
		ClientManager.shutdown();
		
		assertFalse(client.isConnected());
	}
	
	@Test
	void shutdown_StopPlugins_Test() {		
		AtomicBoolean pluginStopped = new AtomicBoolean(false);
		
		Plugin plugin = new Plugin("test",new ServerdPlugin() {
			@Override
			public String init(Plugin plugin) 
			{
				return INIT_SUCCESS;
			}
			
			@Override
			public void work(Plugin plugin) {}
			
			@Override
			public void stop(Plugin plugin) 
			{
				pluginStopped.set(true);
			}
			
			@Override
			public void metadata(Info info) {}
			
		});
		
		PluginManager.addPlugin(plugin);
		ClientManager.shutdown();
		
		assertTrue(pluginStopped.get());
	}
	
	@Test
	void getAllClients_Test() {
		int count = 10;
		for (int i = 0;i < count;i++)
			ClientManager.addClient(new Client(i));
		
		Client[] clients = ClientManager.getAllClients();
		assertEquals(clients.length, count);
		
		for (int i = 0;i < count;i++)
			assertTrue(ClientManager.clients.containsValue(clients[i]));
	}
	
	@Test
	void getFreeClientID_Test() {
		for (int i = 0;i < 10;i++) {
			ClientManager.addClient(new Client(ClientManager.getFreeClientID()));
		}
		
		assertEquals(ClientManager.getFreeClientID(), 10);
		
		ClientManager.delete(5);
		
		assertEquals(ClientManager.getFreeClientID(), 5);
	}
}
