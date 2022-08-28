package com.serverd.plugin;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.serverd.plugin.Plugin.Info;

class DebugTest 
{	
	
	@Test
	void loadPluginFromClassName_Test()
	{
		assertDoesNotThrow(() -> Debug.loadPluginFromClassName(TestPlugin2.class.getName()));
	}
	
	@Test
	void testPlugin_Test() 
	{
		assertDoesNotThrow(() -> {
			String serverdPath = new File(Debug.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
			String testPath = new File(DebugTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
			
			System.setProperty("java.class.path", System.getProperty("java.class.path") + ":" + serverdPath + ":" + testPath);
			
			assertEquals(Debug.testPlugin(TestPlugin1.class.getName(), false, new String[] {}),0);
		});
	}

}

class TestPlugin1 implements ServerdPlugin
{
	public TestPlugin1() {}
	@Override
	public void metadata(Info info) {}

	@Override
	public String init(Plugin plugin)
	{
		return null;
	}

	@Override
	public void work(Plugin plugin) 
	{
		System.exit(0);
	}

	@Override
	public void stop(Plugin plugin) {}
	
}

class TestPlugin2 implements ServerdPlugin
{
	public TestPlugin2() {}
	@Override
	public void metadata(Info info) {}

	@Override
	public String init(Plugin plugin)
	{
		return null;
	}

	@Override
	public void work(Plugin plugin) {}

	@Override
	public void stop(Plugin plugin) {}
}
