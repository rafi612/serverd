package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ToTest extends DoubleClientCommandTestCase 
{
	To toCommand = new To();

	@Test
	void executeTest() throws Exception
	{
		String receive = "Test",send = "TestResponse";
		//execute /to command
		executeTest(
				toCommand, 
				args(String.valueOf(testClient2.getID()),send),
				testClient);
		
		//simulate receive message by second client
		testClient2.insertReceive(receive);
		testClient2.executeCommand(receive);
		
		assertEquals(testClient2.getSend()[0],send);
		assertEquals(testClient2.receive(),receive);
	}

}