package com.serverd.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClientTest 
{
	static Client client,client2;
		
	private static class TestClient extends Client
	{
		public TestClient(int id)
		{
			super(id);
		}
		
		@Override
		public boolean checkArgs(String[] args,int length)
		{
			return super.checkArgs(args, length);
		}
	}
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception 
	{
		client = new TestClient(0);
		client2 = new TestClient(1);
		
		ClientManager.clients.add(client);
		ClientManager.clients.add(client2);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception 
	{
		ClientManager.clients.clear();
	}


	@Test
	void join_Test() 
	{
		//client of of range
		assertEquals(client.join(10),1);
		
		//correct join
		assertEquals(client.join(1),0);
		
		//client already joined
		assertEquals(client.join(1),2);
		
		assertEquals(client.joinedid,client2.id);
		assertEquals(client2.joinedid,client.id);
	
		assertEquals(client.joiner,client2);
		assertEquals(client2.joiner,client);
		
		assertEquals(client.type, Client.Type.SENDER);
		assertEquals(client2.type, Client.Type.RECEIVER);
	}
	
	@Test
	void unjoin_Test()
	{
		client.unjoin();
		
		assertEquals(client.joinedid,-1);
		assertEquals(client2.joinedid,-1);
	
		assertEquals(client.joiner,null);
		assertEquals(client2.joiner,null);
		
		assertEquals(client.type, Client.Type.NONE);
		assertEquals(client2.type, Client.Type.NONE);
	}
	
	@Test
	void checkArgs_Test()
	{
		String[] args1 = {"Test","test","test","test"};
		String[] args2 = {"Test","test"};
		String[] args3 = {"Test","test","test","test","Test","test","test","test"};
		
		assertTrue(client.checkArgs(args1, 4));
		assertFalse(client.checkArgs(args2, 3));
		assertFalse(client.checkArgs(args3, 5));
	}
	
	@Test
	void setOnceJoin_Test()
	{		
		try 
		{
			client.setOnceJoin(true,1);
			
			//simulating receiving response
			client.executeCommand("Test");
			
			assertEquals(client.joinedid,-1);
			assertEquals(client2.joinedid,-1);
		
			assertEquals(client.joiner,null);
			assertEquals(client2.joiner,null);
			
			assertEquals(client.type, Client.Type.NONE);
			assertEquals(client2.type, Client.Type.NONE);
			
		} 
		catch (Exception e) 
		{
			fail(e.getMessage());
		}
	}

}
