package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PluginCommandTest extends CommandTestCase
{
	PluginCommand pluginCommand = new PluginCommand();

	@Test
	void executeTest_PluginNotExists() throws Exception
	{
		executeTest(pluginCommand,
				args("enable","Test"),
				testClient);
		
		assertEquals(testClient.getSend()[0], "ERROR Not found");
	}
}