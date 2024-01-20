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
		
	private static class TestClient extends Client {
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

//	@Test
//	void join_Test() {
//		assertAll(
//			() -> assertDoesNotThrow(() -> client.join(client2.getID())),
//
//			() -> assertEquals(client.getJoinedID(),client2.getID()),
//			() -> assertEquals(client2.getJoinedID(),client.getID())
//		);
//	}
	
//	@Test
//	void join_ClientOutOfRange_Test() {
//		assertThrows(Client.JoinException.class, () -> client.join(10));
//	}
//
//	@Test
//	void join_ClientAlreadyJoined_Test() {
//		assertDoesNotThrow(() -> client.join(client2.getID()));
//		assertThrows(Client.JoinException.class, () -> client.join(client2.getID()));
//	}
	
//	@Test
//	void unjoin_Test() {
//		assertDoesNotThrow(() -> client.join(client2.getID()));
//		client.unjoin();
//
//		assertAll(
//			() -> assertEquals(client.getJoinedID(),-1),
//			() -> assertEquals(client2.getJoinedID(),-1)
//		);
//	}
	
	@Test
	void crash_Test() {
		Client client = new Client(0,clientManager);

		clientManager.addClient(client);
		client.crash(new IOException("Test"));
		
		assertNull(clientManager.getClient(0));
	}
	
//	@Test
//	void crash_WhenJoinedUnjoin_Test() {
//		Client client = new Client(0,clientManager);
//
//		clientManager.addClient(client);
//
//		assertDoesNotThrow(() -> client.join(client2.getID()));
//		client.crash(new IOException("Test"));
//
//		assertFalse(client.isJoined());
//	}
}
