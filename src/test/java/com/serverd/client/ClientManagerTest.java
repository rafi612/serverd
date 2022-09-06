package com.serverd.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.serverd.util.Util;

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
	}

	@AfterEach
	void tearDown() throws Exception
	{
		ClientManager.tcpRunned = false;
		
		ClientManager.clients.clear();
	}
	
	@Test
	void tcpServer_Test()
	{
		assumeTrue(availableTCP(9999));
		
		new Thread(() -> ClientManager.startTcpServer("0.0.0.0", 9999)).start();
		
		Util.sleep(1000);
		
		//checking client connection
		assertDoesNotThrow(() -> {
			try (Socket sock = new Socket("0.0.0.0",9999)) 
			{
				//send test command
				sock.getOutputStream().write("/id".getBytes());
				
				sock.getInputStream().read();
				
				sock.getOutputStream().write("/disconnect".getBytes());
			}
		});
		assertFalse(availableTCP(9999));
		
		assertDoesNotThrow(ClientManager::stopTcpServer);
	}
	
	@Test
	void udpServer_Test()
	{
		assumeTrue(availableUDP(9998));
		
		new Thread(() -> ClientManager.startUdpServer("0.0.0.0", 9998)).start();
		
		Util.sleep(1000);
		
		//checking client connection
		assertDoesNotThrow(() -> {
			
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
		});
		assertFalse(availableUDP(9998));
		
		assertDoesNotThrow(ClientManager::stopUdpServer);
	}
	

}
