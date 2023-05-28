package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JoinTest extends DoubleClientCommandTestCase {
	Join joinCommand = new Join();

	@Test
	void executeTest() throws Exception {
		executeTest(
				joinCommand,
				args(String.valueOf(testClient2.getID())), 
				testClient);
		
		assertAll(
			() -> assertEquals(testClient.getSend()[0],"OK"),
			() -> assertTrue(testClient.isJoined()),
			() -> assertTrue(testClient2.isJoined())
		);
		
	}
	
	@Test
	void executeTest_ClientNotExists() throws Exception {
		executeTest(
				joinCommand,
				args("10"), 
				testClient);
		
		assertTrue(testClient.getSend()[0].contains("ERROR"));
	}
}
