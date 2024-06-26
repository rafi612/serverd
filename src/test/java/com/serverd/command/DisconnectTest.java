package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DisconnectTest extends CommandTestCase {
	Disconnect disconnectCommand = new Disconnect();
	
	@Test
	void executeTest() throws Exception {
		executeTest(disconnectCommand, testClient);
		
		assertAll(
			() -> assertFalse(testClient.isConnected()),
			() -> assertNull(testClient.getClientManager().getClient(testClient.getId()))
		);
	}

}
