package com.serverd.plugin;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.serverd.main.Main;
import com.serverd.plugin.Plugin.Info;

class DebugTest 
{	
	@TempDir
	File testWorkdir;
	
	@Test
	void loadPluginFromClassName_Test()
	{
		System.setProperty("inside.test", "true");
		assertDoesNotThrow(() -> Debug.loadPluginFromClassName(DebugTestPlugin.class.getName()));
	}
	
	@Test
	void testPlugin_withoutPluginsEnabled_Test() 
	{
		assertDoesNotThrow(() -> {
			String serverdPath = new File(Debug.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
			String testPath = new File(DebugTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
			
			System.setProperty("java.class.path", System.getProperty("java.class.path") + ":" + serverdPath + ":" + testPath);
			
			String[] args = {"--working-loc",testWorkdir.getPath()};
			assertEquals(Debug.testPlugin(DebugTestPlugin.class.getName(), false, args),0);
		});
	}
	
	@Test
	void testPlugin_withPluginsEnabled_Test() 
	{
		assertDoesNotThrow(() -> {
			String serverdPath = new File(Debug.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
			String testPath = new File(DebugTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
			
			System.setProperty("java.class.path", System.getProperty("java.class.path") + ":" + serverdPath + ":" + testPath);
			
			String[] args = {"--working-loc",testWorkdir.getPath()};
			assertEquals(Debug.testPlugin(DebugTestPlugin.class.getName(), true, args),0);
		});
	}
	
	@Test
	void testPlugin_withArgsNull_Test() 
	{
		File defaultWorkdir = new File(Main.getWorkDir());
		assumeTrue(defaultWorkdir.exists() || defaultWorkdir.mkdir());
		
		assertDoesNotThrow(() -> {
			String serverdPath = new File(Debug.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
			String testPath = new File(DebugTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
			
			System.setProperty("java.class.path", System.getProperty("java.class.path") + ":" + serverdPath + ":" + testPath);
			
			assertEquals(Debug.testPlugin(DebugTestPlugin.class.getName(), false, null),0);
		});
	}
}

class DebugTestPlugin implements ServerdPlugin
{
	public DebugTestPlugin() {}
	@Override
	public void metadata(Info info) {}

	@Override
	public String init(Plugin plugin)
	{
		return INIT_SUCCESS;
	}

	@Override
	public void work(Plugin plugin) 
	{
		if (System.getProperty("inside.test") == null)
			System.exit(0);
	}

	@Override
	public void stop(Plugin plugin) {}	
}
