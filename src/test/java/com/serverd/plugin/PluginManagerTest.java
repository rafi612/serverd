package com.serverd.plugin;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.serverd.plugin.Plugin.Info;

class PluginManagerTest
{
	Plugin plugin;
	int pluginid;
	
	@Nested
	class LoadPluginsFromFile
	{
		@TempDir
		File testPluginDir;
		
		File jarFile;
		
		@BeforeEach
		void setUp()
		{
			PluginManager.pluginDir = testPluginDir.getPath();
			jarFile = new File(testPluginDir,"TestPlugin.jar");
		}
		
		@Test
		void loadPlugins_Test() 
		{
			assertDoesNotThrow(PluginManager::loadPlugins);
		}
		
		@Test
		void loadPlugins_withPluginFile_Test() throws FileNotFoundException, IOException 
		{
			createPluginFile(jarFile, true, PluginManagerTestPlugin.class.getName(), true, true);
			
			assertDoesNotThrow(PluginManager::loadPlugins);
		}
		
		@Test
		void loadPlugins_withPluginFilecauseError_Test() throws FileNotFoundException, IOException 
		{
			createPluginFile(jarFile, false, "test.class", true, true);
			
			assertDoesNotThrow(PluginManager::loadPlugins);
		}
		
		
		@Test
		void load_Test() throws FileNotFoundException, IOException 
		{
			
			createPluginFile(jarFile, true, PluginManagerTestPlugin.class.getName(), true, true);
			
			assertDoesNotThrow(() -> PluginManager.load(jarFile, true));
		}
		
		@Test
		void load_notEnabled_Test() throws FileNotFoundException, IOException 
		{			
			createPluginFile(jarFile, true, PluginManagerTestPlugin.class.getName(), true, true);
			
			assertDoesNotThrow(() -> PluginManager.load(jarFile, false));
		}
		
		@Test
		void load_pluginMainClassNotExists_Test() throws FileNotFoundException, IOException 
		{			
			createPluginFile(jarFile, false, "test.class", true, true);
			
			var exception = assertThrows(PluginLoadException.class,() -> PluginManager.load(jarFile, true));
			assertTrue(exception.getCause() instanceof ClassNotFoundException);
		}
		
		@Test
		void load_noManifestExists_Test() throws FileNotFoundException, IOException 
		{			
			createPluginFile(jarFile, true, PluginManagerTestPlugin.class.getName(), false, false);
			
			var exception = assertThrows(PluginLoadException.class,() -> PluginManager.load(jarFile, true));
			assertTrue(exception.getCause() instanceof Exception);
		}
		
		@Test
		void load_noManifestPluginMainClassEntry_Test() throws FileNotFoundException, IOException 
		{			
			createPluginFile(jarFile, true, PluginManagerTestPlugin.class.getName(), true, false);
			
			assertThrows(PluginLoadException.class,() -> PluginManager.load(jarFile, false));
		}
		
		void createPluginFile(File file,boolean mainClass,String mainClassName,boolean hasManifest,boolean mainClassManifest) throws FileNotFoundException, IOException
		{
			try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file)))
			{
				if (mainClass)
				{
					//plugin class
					String pluginClassFile = mainClassName.replace(".", "/") + ".class";
					
					ZipEntry classZipEntry = new ZipEntry(pluginClassFile);
					zout.putNextEntry(classZipEntry);
					
					byte[] classByte = PluginManagerTest.class.getResourceAsStream("/" + pluginClassFile).readAllBytes();
					zout.write(classByte, 0, classByte.length);
					
					zout.closeEntry();
				}
				
				if (hasManifest) 
				{
					//manifest
					String manifest = "Manifest-Version: 1.0\n" 
							+ (mainClassManifest ? ("Plugin-Main-Class: " + mainClassName + "\n") : "");
					ZipEntry manifestZipEntry = new ZipEntry(JarFile.MANIFEST_NAME);
					zout.putNextEntry(manifestZipEntry);
					zout.write(manifest.getBytes());
					zout.closeEntry();
				}
			}
		}
	}
	
	@BeforeEach
	void setUp() throws Exception
	{
		PluginManager.init();
		
		pluginid = Debug.loadPluginFromClassName(PluginManagerTestPlugin.class.getName());
		plugin = PluginManager.getPluginByID(pluginid);
	}
	
	@AfterEach
	void tearDown() throws Exception
	{
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
		PluginManager.enablePlugin(plugin);
		assertTrue(plugin.isRunned());
	}

}

class PluginManagerTestPlugin implements ServerdPlugin
{
	@Override
	public void metadata(Info info) {}

	@Override
	public String init(Plugin plugin)
	{
		return INIT_SUCCESS;
	}

	@Override
	public void work(Plugin plugin) {}

	@Override
	public void stop(Plugin plugin) {}	
}
