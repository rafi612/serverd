package com.serverd.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.jupiter.api.Test;

import com.serverd.util.Util;

class ClientManagerTest 
{
	
	private static boolean availableTCP(int port) 
	{
	    try (ServerSocket server = new ServerSocket(port,50,InetAddress.getByName("0.0.0.0")))
	    {
	        return true;
	    } catch (IOException ignored) {
	        return false;
	    }
	}
	
	private static boolean availableUDP(int port) 
	{
	    try (DatagramSocket socket = new DatagramSocket(port)) {
	        return true;
	    } catch (IOException ignored) {
	        return false;
	        
	    }
	}

//	@Test
//	void start_Test() 
//	{
//		ClientManager.start("0.0.0.0", 9999, 9998);
//		
//		
//		
//		assertEquals(availableTCP(9999), false);
//		assertEquals(availableUDP(9998), false);
//	}
//	
//	@Test
//	void tcp_server_Test()
//	{
//		new Thread(() -> ClientManager.tcp_server("0.0.0.0", 9999)).start();
//		
//		Util.sleep(1000);
//		
//		assertEquals(availableTCP(9999), false);
//	}
//	
//	@Test
//	void udp_server_Test()
//	{
//		ClientManager.udp_server("0.0.0.0", 9998);
//		
//		assertEquals(availableUDP(9998), false);
//	}

}
