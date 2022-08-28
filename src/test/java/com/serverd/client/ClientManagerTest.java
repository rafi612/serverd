package com.serverd.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;

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

	@Test
	void start_Test() 
	{
		ClientManager.start("0.0.0.0", 9999, 9998);
		
		Util.sleep(1000);
		
		assertFalse(availableTCP(9999));
		assertFalse(availableUDP(9998));
	}
	

}
