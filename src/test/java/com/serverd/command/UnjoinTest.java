package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UnjoinTest extends DoubleClientCommandTestCase
{
	Unjoin unjoinCommand = new Unjoin();
	@Test
	void executeTest() throws Exception 
	{
		testClient.join(testClient2.getID());
		
		executeTest(unjoinCommand, testClient);
		
		assertAll(
			() -> assertEquals(testClient.getSend()[0], "OK"),
			() -> assertFalse(testClient.isJoined()),
			() -> assertFalse(testClient2.isJoined())
		);
	}
}
