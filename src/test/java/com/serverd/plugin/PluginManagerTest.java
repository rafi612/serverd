package com.serverd.plugin;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.serverd.main.Main;

class PluginManagerTest {

	@TempDir
	File tempDir;
	
	Plugin plugin;
	int pluginid;
	
	@BeforeEach
	void setUp() throws Exception
	{
		PluginManager.init();
		
		pluginid = Debug.loadPluginFromClassName(TestPlugin2.class.getName());
		plugin = PluginManager.getPluginByID(pluginid);
	}
	
	@AfterEach
	void tearDown() throws Exception
	{
		PluginManager.unloadPlugin(plugin);
	}
	
	@Test
	void loadPlugins_Test() 
	{
		Main.workingdir = tempDir.getAbsolutePath();
		assertDoesNotThrow(PluginManager::loadPlugins);
		
		PluginManager.unloadAllPlugins();
	}
	
	@Test
	void getByFileName_Test()
	{
		assertNotNull(PluginManager.getByFileName(plugin.filename));
	}
	
	@Test
	void getPluginByID_Test()
	{
		assertNotNull(PluginManager.getPluginByID(0));
	}
	
	@Test
	void shouldGetPluginByIDReturnNullOnWrongID()
	{
		assertNull(PluginManager.getPluginByID(10));
	}

	@Test
	void shouldGetByFileNameReturnNullOnWrongName()
	{
		assertNull(PluginManager.getByFileName("Test"));
	}
	
	@Test
	void listPluginsName_Test()
	{
		assertAll(
			() -> assertEquals(PluginManager.listPluginsName().length,PluginManager.getPluginsAmountLoaded()),
			() -> assertEquals(PluginManager.listPluginsName()[pluginid],plugin.filename)
		);
	}
	
	@Test
	void disablePlugin_Test()
	{
		PluginManager.disablePlugin(plugin);
		
		assertFalse(plugin.isRunned());
	}
	
	@Test
	void EnablePlugin_Test()
	{
		PluginManager.disablePlugin(plugin);
		assertFalse(plugin.isRunned());
		
		PluginManager.enablePlugin(plugin);
		assertTrue(plugin.isRunned());
	}

}
