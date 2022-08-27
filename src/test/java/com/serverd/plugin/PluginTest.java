package com.serverd.plugin;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.serverd.plugin.Plugin.Info;

class PluginTest 
{
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
		plugin = new Plugin(new File("Test"), instance = new TestPlugin());
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
		
		assertEquals(plugin.isRunned, false);
		assertEquals(instance.stopped, true);
	}
	
	@Test
	void getInstance_Test()
	{
		assertEquals(plugin.getInstance(), instance);
	}

}
