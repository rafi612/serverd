package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IdTest extends CommandTestCase 
{
	Id idCommand = new Id();
	@Test
	void executeTest() throws Exception 
	{
		executeTest(idCommand, testClient);
		
		assertEquals(Integer.parseInt(testClient.getSend()[0]), testClient.getID());
	}

}
