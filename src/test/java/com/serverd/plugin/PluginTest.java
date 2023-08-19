package com.serverd.plugin;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.serverd.plugin.Plugin.Info;

class PluginTest 
{
	@TempDir
	static File tempDir;
	
	Plugin plugin;
	TestPlugin instance;
	
	private static class TestPlugin implements ServerdPlugin {
		public Action action = Action.OK;
		public enum Action {
			OK,ERROR,EMPTY_STRING,NULL
		}
		public boolean stopped = false;
		
		@Override
		public void metadata(Info info) {}

		@Override
		public String init(Plugin plugin) {
			switch (action) {
				case ERROR:
					return "Error";
				case EMPTY_STRING:
					return "";
				case NULL:
					return null;
				default:
					return INIT_SUCCESS;
			}
		}

		@Override
		public void work(Plugin plugin) {}

		@Override
		public void stop(Plugin plugin) {
			stopped = true;
		}
	}

	@BeforeEach
	void setUp() throws Exception {
		plugin = new Plugin("test",instance = new TestPlugin());
		plugin.getInfo().name = "Test";
	}
	
	@Test
	void start_Test() {
		assertTrue(plugin.start());
	}

	
	@Test
	void start_nullReturnOnInit_Test() {
		instance.action = TestPlugin.Action.NULL;
		assertTrue(plugin.start());
	}
	
	@Test
	void start_emptyStringReturnOnInit_Test() {
		instance.action = TestPlugin.Action.EMPTY_STRING;
		assertTrue(plugin.start());
	}
	
	@Test
	void start_errorOnInit_Test() {
		instance.action = TestPlugin.Action.ERROR;
		assertFalse(plugin.start());
	}

	@Test
	void stop_Test() {
		plugin.stop();
		
		assertAll(
			() -> assertEquals(plugin.isRunned(), false),
			() -> assertEquals(instance.stopped, true)
		);
	}
	
	@Test
	void getInstance_Test() {
		assertEquals(plugin.getInstance(), instance);
	}
	
	@Test
	void loadResource_Test() {
		//loads self class for test
		String resourcePath = "/" + PluginTest.class.getName().replace(".", "/") + ".class";
		assertNotNull(plugin.loadResource(resourcePath));
	}
	
	//First repeat create workspace, second repeat load exists workspace
	@RepeatedTest(2)
	void loadWorkspace_Test() {
		PluginManager.pluginDataDir = tempDir;
		
		File workspace = plugin.loadWorkspace();
		
		assertEquals(workspace.getAbsolutePath(), new File(PluginManager.pluginDataDir, plugin.getInfo().name).getAbsolutePath());
	}
}
