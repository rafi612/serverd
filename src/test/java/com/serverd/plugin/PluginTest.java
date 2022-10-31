package com.serverd.plugin;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;

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
	
	private class TestPlugin implements ServerdPlugin
	{
		public boolean stopped = false;
		
		@Override
		public void metadata(Info info) 
		{
			
		}

		@Override
		public String init(Plugin plugin)
		{
			return "Error";
		}

		@Override
		public void work(Plugin plugin) 
		{
			
		}

		@Override
		public void stop(Plugin plugin) 
		{
			stopped = true;
		}
		
	}

	@BeforeEach
	void setUp() throws Exception 
	{
		plugin = new Plugin("test",instance = new TestPlugin());
		plugin.getInfo().name = "Test";
	}

	@Test
	void start_Test()
	{
		assertEquals(plugin.start(), 1);
	}
	
	@Test
	void stop_Test()
	{
		plugin.stop();
		
		assertAll(
			() -> assertEquals(plugin.isRunned(), false),
			() -> assertEquals(instance.stopped, true)
		);
	}
	
	@Test
	void getInstance_Test()
	{
		assertEquals(plugin.getInstance(), instance);
	}
	
	//First repeat create workspace, second repeat load exists workspace
	@RepeatedTest(value = 2)
	void loadWorkspace_Test()
	{
		PluginManager.pluginDataDir = tempDir.getAbsolutePath();
		
		File workspace = plugin.loadWorkspace();
		
		assertEquals(workspace.getAbsolutePath(), Path.of(PluginManager.pluginDataDir, plugin.getInfo().name).toString());
	}

}
