package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StatusTest extends CommandTestCase {
	Status statusCommand = new Status();
	
	@Test
	void executeTest() throws Exception {
		executeTest(statusCommand, testClient);
		
		//checking amount of lines in status message, one line is one client
		assertEquals(testClient.getSend()[0].split("\n").length, clientManager.getClientConnectedAmount());
	}
	
	@Test
	void executeTest_NoClients() throws Exception {
		clientManager.delete(testClient.getId());
		
		executeTest(statusCommand, testClient);
		
		assertEquals(testClient.getSend()[0], "No clients connected");
	}
}
