package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SetnameTest extends CommandTestCase 
{
	Setname setnameCommand = new Setname();
	
	@Test
	void executeTest() throws Exception
	{
		String name = "ClientTestName";
		
		executeTest(setnameCommand, args(name), testClient);
		assertEquals(testClient.getName(), name);
	}
}
