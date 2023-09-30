package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HelpTest extends CommandTestCase {
	Help helpCommand = new Help();
	
	@Test
	void executeTest() throws Exception {
		//only this command is added to build in commands list
		executeTest(helpCommand, testClient);
		
		//checking if only this command exists in build in commands list
		assertEquals(testClient.getSend()[0].split("\n").length, 1);
	}
}
