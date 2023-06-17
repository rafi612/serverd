package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HelpTest extends CommandTestCase {
	Help helpCommand = new Help();
	
	@Test
	void executeTest() throws Exception {
		//only this command is added to buildin commands list
		executeTest(helpCommand, testClient);
		
		//checking if only this command exists in buildin commands list
		assertEquals(testClient.getSend()[0].split("\n").length, 1);
	}
}
