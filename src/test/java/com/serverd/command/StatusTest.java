package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.serverd.client.ClientManager;

class StatusTest extends CommandTestCase 
{
	Status statucCommand = new Status();
	
	@Test
	void executeTest() throws Exception 
	{
		executeTest(statucCommand, testClient);
		
		//checking amount of lines in status message, one line is one client
		assertEquals(testClient.getSend()[0].split("\n").length, ClientManager.getClientConnectedAmount());
	}
	
	@Test
	void executeTest_NoClients() throws Exception 
	{
		ClientManager.delete(testClient.getID());
		executeTest(statucCommand, testClient);
		
		assertEquals(testClient.getSend()[0], "No clients connected");
	}
}
