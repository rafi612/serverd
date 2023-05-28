package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.serverd.client.ClientManager;

class DisconnectTest extends CommandTestCase {
	Disconnect diconnectCommand = new Disconnect();
	
	@Test
	void executeTest() throws Exception {
		executeTest(diconnectCommand, testClient);
		
		assertAll(
			() -> assertFalse(testClient.isConnected()),
			() -> assertNull(ClientManager.getClient(testClient.getID()))
		);
	}

}
