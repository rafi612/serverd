package com.serverd.plugin;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.serverd.main.Main;

class PluginManagerTest {

	@TempDir
	File tempDir;
	
	@Test
	void loadPlugins_Test() 
	{
		try
		{
			Main.workingdir = tempDir.getAbsolutePath();
			PluginManager.loadPlugins();
		} 
		catch (IOException e) 
		{
			fail(e.getMessage());
		}
	}

}
