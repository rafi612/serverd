package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CloseTest extends CommandTestCase {
	Close closeCommand = new Close();
	
	@Test
	void executeTest() throws Exception {
		executeTest(
				closeCommand, 
				args(Integer.toString(testClient.getID())),
				testClient);
		
		assertEquals(testClient.getSend()[0], "OK");
	}
	
	@Test
	void executeTest_ClientNotExists() throws Exception {
		executeTest(
				closeCommand, 
				args("10"),
				testClient);
		
		assertEquals(testClient.getSend()[0], "ERROR client not found");
	}
}