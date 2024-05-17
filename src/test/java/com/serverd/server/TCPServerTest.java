package com.serverd.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.serverd.app.ServerdApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TCPServerTest {

	ServerdApplication app;

	@BeforeEach
	void setUp() {
		app = new ServerdApplication();
	}

	private static boolean availableTcp(int port) {
		try (ServerSocket server = new ServerSocket(port,50,InetAddress.getByName("0.0.0.0"))) {
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}
	
	
	@Test
	void startTcpServer_Test() throws IOException, InterruptedException {
		TCPServer server = new TCPServer("0.0.0.0",9999,app.getClientManager(),0);
		server.setApp(app);
		server.setServerManager(app.getServerManager());
		
	    assumeTrue(availableTcp(9999));

	    new Thread(() -> {
	        try {
				server.start();
			} catch (IOException e) {
				fail("Start error: " + e.getMessage());
			}
	    }).start();

	    while (availableTcp(9999))
	        Thread.sleep(100);

	    try (Socket clientSocket = new Socket("localhost", 9999)) {
	        clientSocket.getOutputStream().write("/id".getBytes());

	        Thread.sleep(100);

	        clientSocket.getOutputStream().write("/disconnect".getBytes());

            assertFalse(availableTcp(9999));
	    } catch (IOException e) {
	        fail("Failed to connect to TCP server: " + e.getMessage());
	    }

	    server.stop();
	}

}
