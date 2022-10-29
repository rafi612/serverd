package com.serverd.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientTest 
{
	Client client,client2;
		
	private static class TestClient extends Client
	{
		public TestClient(int id)
		{
			super(id);
		}
		
		@Override
		public boolean checkArgs(String[] args,int length) throws IOException
		{
			return super.checkArgs(args, length);
		}
	}
	
	@BeforeEach
	void setUp() throws Exception 
	{
		client = new TestClient(0);
		client2 = new TestClient(1);
		
		ClientManager.addClient(client);
		ClientManager.addClient(client2);
	}

	@AfterEach
	void tearDown() throws Exception 
	{
		ClientManager.clients.clear();
	}


	@Test
	void join_Test() 
	{
		assertAll(
			() -> assertDoesNotThrow(() -> client.join(client2.getID())),
			
			() -> assertEquals(client.joinedid,client2.id),
			() -> assertEquals(client2.joinedid,client.id),
		
			() -> assertEquals(client.joiner,client2),
			() -> assertEquals(client2.joiner,client),
			
			() -> assertEquals(client.type, Client.Type.SENDER),
			() -> assertEquals(client2.type, Client.Type.RECEIVER)
		);
	}
	
	@Test
	void join_ClientOutOfRange_Test() 
	{
		assertThrows(Client.JoinException.class, () -> client.join(10));
	}
	
	@Test
	void join_ClientAlreadyJoined_Test()
	{
		assertDoesNotThrow(() -> client.join(client2.getID()));
		assertThrows(Client.JoinException.class, () -> client.join(client2.getID()));
	}
	
	@Test
	void unjoin_Test()
	{
		assertDoesNotThrow(() -> client.join(client2.getID()));
		client.unjoin();
		
		assertAll(
			() -> assertEquals(client.joinedid,-1),
			() -> assertEquals(client2.joinedid,-1),
			
			() -> assertNull(client.joiner),
			() -> assertNull(client2.joiner),
				
			() -> assertEquals(client.type, Client.Type.NONE),
			() -> assertEquals(client2.type, Client.Type.NONE)
		);
	}
	
	@Test
	void checkArgs_Test()
	{
		String[] args1 = {"Test","test","test","test"};
		String[] args2 = {"Test","test"};
		String[] args3 = {"Test","test","test","test","Test","test","test","test"};
		
		assertAll(
			() -> assertTrue(client.checkArgs(args1, 4)),
			() -> assertFalse(client.checkArgs(args2, 3)),
			() -> assertFalse(client.checkArgs(args3, 5))
		);
	}
	
	@Test
	void onceJoin_Test()
	{		
		assertDoesNotThrow(() -> {
			client.onceJoin(client2.getID());
			
			//simulating receiving response
			client.executeCommand("Test");
			
			assertAll(
				() -> assertEquals(client.joinedid,-1),
				() -> assertEquals(client2.joinedid,-1),
				
				() -> assertNull(client.joiner),
				() -> assertNull(client2.joiner),
					
				() -> assertEquals(client.type, Client.Type.NONE),
				() -> assertEquals(client2.type, Client.Type.NONE)
			); 
		});
	}
	
	@Test
	void onceJoin_SelfJoin_Test()
	{		
		assertThrows(Client.JoinException.class,() -> client.onceJoin(client.getID()));
	}
	
	@Test
	void crash_Test()
	{
		Client client = new Client(0) {
			@Override
			public String receive() throws IOException
			{
				throw new IOException("Test");
			}
		};
		
		ClientManager.addClient(client);
		
		client.run();
		
		assertNull(ClientManager.getClient(0));
		
	}
	
	@Test
	void crash_WhenJoinedUnjoin_Test()
	{
		Client client = new Client(0) {
			@Override
			public String receive() throws IOException
			{
				throw new IOException("Test");
			}
		};
		
		ClientManager.addClient(client);
		
		assertDoesNotThrow(() -> client.join(client2.getID()));
		client.run();
		
		assertFalse(client.isJoined());
		
	}

}
