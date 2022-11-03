package com.serverd.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.ServerdPlugin;
import com.serverd.plugin.listener.ConnectListener;
import com.serverd.plugin.Plugin.Info;

class ClientManagerTest 
{
	
	private static boolean availableTCP(int port) 
	{
		try (ServerSocket server = new ServerSocket(port,50,InetAddress.getByName("0.0.0.0")))
		{
			return true;
		} 
		catch (IOException ignored)
		{
			return false;
		}
	}
	
	private static boolean availableUDP(int port) 
	{
		try (DatagramSocket socket = new DatagramSocket(port)) 
		{
			return true;
		} 
		catch (IOException ignored) 
		{
			return false;
		}
	}
	
	@BeforeEach
	void setUp() throws Exception
	{
		ClientManager.tcpRunned = true;
		ClientManager.clientsConnected = 0;
	}

	@AfterEach
	void tearDown() throws Exception
	{
		ClientManager.tcpRunned = false;
		
		ClientManager.clients.clear();
	}
	
	@Test
	void tcpServer_Test() throws InterruptedException, UnknownHostException, IOException
	{
		assumeTrue(availableTCP(9999));
		
		new Thread(() -> ClientManager.startTcpServer("0.0.0.0", 9999)).start();
		
		while (availableTCP(9999))
			Thread.sleep(100);
		
		//checking client connection
		try (Socket sock = new Socket("0.0.0.0",9999)) 
		{
			//send test command
			sock.getOutputStream().write("/id".getBytes());
			
			sock.getInputStream().read();
			
			sock.getOutputStream().write("/disconnect".getBytes());
		}
		
		assertFalse(availableTCP(9999));
		
		ClientManager.stopTcpServer();
	}
	
	@Test
	void udpServer_Test() throws InterruptedException, IOException
	{
		assumeTrue(availableUDP(9998));
		
		new Thread(() -> ClientManager.startUdpServer("0.0.0.0", 9998)).start();
		
		while (availableUDP(9998))
			Thread.sleep(100);
		
		//checking client connection
		try (DatagramSocket sock = new DatagramSocket()) 
		{
			//send test command
			String msg = "/id";
			DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), InetAddress.getByName("0.0.0.0"), 9998);
			
			sock.send(packet);
			
			byte[] buffer = new byte[65565];
			DatagramPacket receive = new DatagramPacket(buffer, buffer.length);
			
			sock.receive(receive);
			
			String msg2 = "/disconnect";
			DatagramPacket packet2 = new DatagramPacket(msg2.getBytes(), msg2.length(), InetAddress.getByName("0.0.0.0"), 9998);
			
			sock.send(packet2);
		}
		assertFalse(availableUDP(9998));
		
		ClientManager.stopUdpServer();
	}
	
	@Test
	void delete_Test()
	{
		int count = 10;
		for (int i = 0;i < count;i++)
			ClientManager.addClient(new Client(i));
		
		ClientManager.delete(5);
		
		assertEquals(ClientManager.clients.size(), 9);
	}
	
	@Test
	void delete_WhenJoinedUnjoin_Test()
	{
		int count = 10;
		for (int i = 0;i < count;i++)
			ClientManager.addClient(new Client(i));
		
		Client client1 = ClientManager.getClient(4);
		Client client2 = ClientManager.getClient(5);
		
		assertDoesNotThrow(() -> client1.join(client2.getID()));
		
		ClientManager.delete(5);
		
		assertAll(
			() -> assertEquals(ClientManager.clients.size(), 9),
			() -> assertFalse(client1.isJoined()),
			() -> assertFalse(client2.isJoined())
		);
	}
	
	@Test
	void delete_IsDisconnectEventExecuting_Test()
	{
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
	void delete_ClientsSizeEqualsZero_Test()
	{
		ClientManager.delete(0);
		
		assertEquals(ClientManager.clientsConnected, 0);
	}
	
	@Test
	void shutdown_StopServer_Test() throws InterruptedException
	{
		assumeTrue(availableTCP(9999));
		assumeTrue(availableUDP(9998));
		
		ClientManager.start("0.0.0.0", 9999, 9998);
		
		while (availableTCP(9999) || availableUDP(9998))
			Thread.sleep(100);
		
		ClientManager.shutdown();
		
		assertAll(
			() -> assertTrue(availableTCP(9999)),
			() -> assertTrue(availableUDP(9998))
		);
	}
	
	@Test
	void shutdown_StopClients_Test()
	{
		
		Client client = new Client(ClientManager.getFreeClientID()) {
			public void run() {
				connected = true;
			}
		};
		client.run();
		ClientManager.addClient(client);
		
		ClientManager.shutdown();
		
		assertFalse(client.isConnected());
	}
	
	@Test
	void shutdown_StopPlugins_Test()
	{		
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
	void getAllClients_Test()
	{
		int count = 10;
		for (int i = 0;i < count;i++)
			ClientManager.addClient(new Client(i));
		
		Client[] clients = ClientManager.getAllClients();
		assertEquals(clients.length, count);
		
		for (int i = 0;i < count;i++)
			assertTrue(ClientManager.clients.containsValue(clients[i]));
	}
	
	@Test
	void getFreeClientID_Test()
	{
		for (int i = 0;i < 10;i++)
		{
			ClientManager.addClient(new Client(ClientManager.getFreeClientID()));
		}
		
		assertEquals(ClientManager.getFreeClientID(), 10);
		
		ClientManager.delete(5);
		
		assertEquals(ClientManager.getFreeClientID(), 5);
	}
}
