package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PluginsListTest extends CommandTestCase {
	PluginsList pluginsListCommand = new PluginsList();

	@Test
	void executeTest_NoPlugins() throws Exception  {
		executeTest(pluginsListCommand, testClient);
		
		assertEquals(testClient.getSend()[0], "No plugins installed");
	}
}
