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
	void setUpBeforeClass() throws Exception 
	{
		client = new TestClient(0);
		client2 = new TestClient(1);
		
		ClientManager.clients.add(client);
		ClientManager.clients.add(client2);
	}

	@AfterEach
	void tearDownAfterClass() throws Exception 
	{
		ClientManager.clients.clear();
	}


	@Test
	void join_Test() 
	{
		assertAll(
			//client of of range
			() -> assertEquals(client.join(10),1),
			
			//correct join
			() -> assertEquals(client.join(1),0),
			
			//client already joined
			() -> assertEquals(client.join(1),2),
			
			() -> assertEquals(client.joinedid,client2.id),
			() -> assertEquals(client2.joinedid,client.id),
		
			() -> assertEquals(client.joiner,client2),
			() -> assertEquals(client2.joiner,client),
			
			() -> assertEquals(client.type, Client.Type.SENDER),
			() -> assertEquals(client2.type, Client.Type.RECEIVER)
		);
	}
	
	@Test
	void unjoin_Test()
	{
		client.join(1);
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
	void setOnceJoin_Test()
	{		
		assertDoesNotThrow(() -> {
			client.setOnceJoin(true,1);
			
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

}