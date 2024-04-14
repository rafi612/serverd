package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JoinTest extends DoubleClientCommandTestCase {
	Join joinCommand = new Join();

	@Test
	void executeTest() throws Exception {
		executeTest(
				joinCommand,
				args(String.valueOf(testClient2.getId())),
				testClient);
		
		assertAll(
			() -> assertEquals(testClient.getSend()[0],"OK"),
			() -> assertTrue(((CommandProcessor)testClient.getProcessor()).isJoined()),
			() -> assertTrue(((CommandProcessor)testClient2.getProcessor()).isJoined())
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
