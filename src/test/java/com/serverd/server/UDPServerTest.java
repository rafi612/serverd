package com.serverd.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.serverd.app.ServerdApplication;
import com.serverd.client.ClientManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UDPServerTest {

	ServerdApplication app;

	@BeforeEach
	void setUp() {
		app = new ServerdApplication();
	}

	
	private static boolean availableUDP(int port) {
		try (DatagramSocket socket = new DatagramSocket(port)) {
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}
	
	@Test
	void startUdpServer_Test() throws InterruptedException, IOException {
		ClientManager clientManager = app.getClientManager();
	    UDPServer server = new UDPServer("0.0.0.0", 9998,clientManager,0);
		server.setApp(app);
		server.setServerManager(app.getServerManager());

	    server.isRunning = true;
	    
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
