package com.serverd.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.jupiter.api.Test;

import com.serverd.config.Config;

class TCPServerTest {
	private static boolean availableTCP(int port) {
		try (ServerSocket server = new ServerSocket(port,50,InetAddress.getByName("0.0.0.0"))) {
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}
	
	
	@Test
	void startTcpServer_Test() throws IOException, InterruptedException {
		TCPServer server = new TCPServer("0.0.0.0",9999,new Config());
		
	    assumeTrue(availableTCP(9999));

	    Thread serverThread = new Thread(() -> {
	        try {
				server.start();
			} catch (IOException e) {
				fail("Start error: " + e.getMessage());
			}
	    });
	    serverThread.start();

	    while (availableTCP(9999))
	        Thread.sleep(100);

	    try (Socket clientSocket = new Socket("localhost", 9999)) {
	        clientSocket.getOutputStream().write("/id".getBytes());

	        Thread.sleep(100);

	        clientSocket.getOutputStream().write("/disconnect".getBytes());

            assertFalse(availableTCP(9999));
	    } catch (IOException e) {
	        fail("Failed to connect to TCP server: " + e.getMessage());
	    }

	    server.stop();
	}

}
