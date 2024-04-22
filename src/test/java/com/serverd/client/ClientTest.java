package com.serverd.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import com.serverd.app.ServerdApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientTest {

	ServerdApplication app;
	Client client,client2;

	ClientManager clientManager;
		
	static class TestClient extends Client {
		public TestClient(int id,ClientManager clientManager) {
			super(id,clientManager);
		}
	}
	
	@BeforeEach
	void setUp() {
		app = new ServerdApplication();
		clientManager = app.getClientManager();

		client = new TestClient(0,clientManager);
		client2 = new TestClient(1,clientManager);

		clientManager.addClient(client);
		clientManager.addClient(client2);
	}

	@AfterEach
	void tearDown() {
		clientManager.clients.clear();
	}

	
	@Test
	void crash_Test() {
		Client client = new Client(0,clientManager);

		clientManager.addClient(client);
		client.crash(new IOException("Test"));
		
		assertNull(clientManager.getClient(0));
	}
}
