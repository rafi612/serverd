package com.serverd.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.junit.jupiter.api.Test;

import com.serverd.config.Config;

class UDPServerTest {
	
	private static boolean availableUDP(int port) {
		try (DatagramSocket socket = new DatagramSocket(port)) {
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}
	
	@Test
	void startUdpServer_Test() throws InterruptedException, IOException {
	    UDPServer server = new UDPServer("0.0.0.0", 9998, new Config());
	    server.isRunned = true;
	    
	    assumeTrue(availableUDP(9998));
	    
	    new Thread(() -> {
			try {
				server.start();
			} catch (IOException e) {
				fail("Start error: " + e.getMessage());
			}
		}).start();
	    
	    Thread.sleep(1000);

	    try (DatagramSocket sock = new DatagramSocket()) {
	        // send test command
	        String msg = "/id";
	        DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), InetAddress.getByName("localhost"), 9998);

	        sock.send(packet);

	        byte[] buffer = new byte[65565];
	        DatagramPacket receive = new DatagramPacket(buffer, buffer.length);

	        sock.receive(receive);

	        String msg2 = "/disconnect";
	        DatagramPacket packet2 = new DatagramPacket(msg2.getBytes(), msg2.length(), InetAddress.getByName("localhost"), 9998);

	        sock.send(packet2);
	    }
	    
	    assertTrue(true);
	}

}
