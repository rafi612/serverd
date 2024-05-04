package com.serverd.plugin;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.serverd.app.DirectorySchema;
import com.serverd.app.ServerdApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.serverd.plugin.Plugin.Info;

class PluginManagerTest {
	Plugin plugin;
	
	@TempDir
	File tempWorkDir;

	ServerdApplication app;
	PluginManager pluginManager;
	
	@Nested
	class LoadPluginsFromFile {
		@TempDir
		File testPluginDir;
		
		File jarFile;
		
		@BeforeEach
		void setUp() {
			app = new ServerdApplication();
			pluginManager = app.getPluginManager();
			pluginManager.unloadAllPlugins();

			pluginManager.pluginDir = testPluginDir;
			pluginManager.pluginsDisabled = List.of();
			jarFile = new File(testPluginDir,"TestPlugin.jar");
		}
		
		@AfterEach
		void tearDown() {
			pluginManager.unloadAllPlugins();
		}
		
		@Test
		void loadPlugins_Test() {
			assertDoesNotThrow(pluginManager::loadPlugins);
		}
		
		@Test
		void loadPlugins_withPluginFile_Test() throws IOException {
			createPluginFile(jarFile, true, PluginManagerTestPlugin.class.getName(), true, true);
			
			assertDoesNotThrow(pluginManager::loadPlugins);
		}
		
		@Test
		void loadPlugins_withPluginFilePluginDisabled_Test() throws IOException {
			pluginManager.pluginsDisabled = List.of(jarFile.getName());
			createPluginFile(jarFile, true, PluginManagerTestPlugin.class.getName(), true, true);
			
			assertDoesNotThrow(pluginManager::loadPlugins);
			assertFalse(pluginManager.getPluginByID(0).isRunning());
		}
		
		@Test
		void loadPlugins_withPluginFileCauseError_Test() throws IOException {
			createPluginFile(jarFile, false, "test.class", true, true);
			
			assertDoesNotThrow(pluginManager::loadPlugins);
		}
		
		@Test
		void load_Test() throws  IOException {
			createPluginFile(jarFile, true, PluginManagerTestPlugin.class.getName(), true, true);
			
			assertDoesNotThrow(() -> pluginManager.load(jarFile, true));
		}
		
		@Test
		void load_notEnabled_Test() throws IOException {
			createPluginFile(jarFile, true, PluginManagerTestPlugin.class.getName(), true, true);
			
			assertDoesNotThrow(() -> pluginManager.load(jarFile, false));
		}
		
		@Test
		void load_pluginMainClassNotExists_Test() throws IOException {
			createPluginFile(jarFile, false, "test.class", true, true);
			
			var exception = assertThrows(PluginLoadException.class,() -> pluginManager.load(jarFile, true));
            assertInstanceOf(ClassNotFoundException.class, exception.getCause());
		}
		
		@Test
		void load_noManifestExists_Test() throws IOException {
			createPluginFile(jarFile, true, PluginManagerTestPlugin.class.getName(), false, false);
			
			var exception = assertThrows(PluginLoadException.class,() -> pluginManager.load(jarFile, true));
            assertInstanceOf(Exception.class, exception.getCause());
		}
		
		@Test
		void load_noManifestPluginMainClassEntry_Test() throws IOException {
			createPluginFile(jarFile, true, PluginManagerTestPlugin.class.getName(), true, false);
			
			assertThrows(PluginLoadException.class,() -> pluginManager.load(jarFile, false));
		}
		
		void createPluginFile(File file,boolean mainClass,String mainClassName,boolean hasManifest,boolean mainClassManifest) throws IOException {
			try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file))) {
				if (mainClass) {
					//plugin class
					String pluginClassFile = mainClassName.replace(".", "/") + ".class";
					
					ZipEntry classZipEntry = new ZipEntry(pluginClassFile);
					zout.putNextEntry(classZipEntry);
					
					byte[] classByte = PluginManagerTest.class.getResourceAsStream("/" + pluginClassFile).readAllBytes();
					zout.write(classByte, 0, classByte.length);
					
					zout.closeEntry();
				}
				
				if (hasManifest) {
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
	void setUp() throws Exception {
		DirectorySchema directorySchema = new DirectorySchema();
		directorySchema.init(tempWorkDir);

		app = new ServerdApplication("test",directorySchema);
		pluginManager = new PluginManager(app);
		pluginManager.unloadAllPlugins();
		pluginManager.init(tempWorkDir,directorySchema);
		
		plugin = PluginUtils.loadPluginFromClassName(PluginManagerTestPlugin.class.getName(),pluginManager);
	}
	
	@AfterEach
	void tearDown() {
		pluginManager.unloadPlugin(plugin);
	}
	
	@Test
	void getByFileName_Test() {
		assertNotNull(pluginManager.getByFileName(plugin.getName()));
	}
	
	@Test
	void getPluginByID_Test() {
		assertNotNull(pluginManager.getPluginByID(0));
	}
	
	@Test
	void getPluginByID_IdLowerThanZero_Test() {
		assertNull(pluginManager.getPluginByID(-1));
	}
	
	@Test
	void getPluginByID_IdGreaterThanMaxPlugin_Test() {
		assertNull(pluginManager.getPluginByID(pluginManager.getPluginsAmountLoaded() + 1));
	}
	
	@Test
	void shouldGetPluginByIDReturnNullOnWrongID() {
		assertNull(pluginManager.getPluginByID(10));
	}

	@Test
	void shouldGetByFileNameReturnNullOnWrongName() {
		assertNull(pluginManager.getByFileName("Test"));
	}
	
	@Test
	void listPluginsName_Test() {
		assertAll(
			() -> assertEquals(pluginManager.listPluginsName().length,pluginManager.getPluginsAmountLoaded()),
			() -> assertEquals(pluginManager.listPluginsName()[pluginManager.getIdByPlugin(plugin)],plugin.getName())
		);
	}
	
	@Test
	void disablePlugin_Test() {
		pluginManager.disablePlugin(plugin);
		
		assertFalse(plugin.isRunning());
	}
	
	@Test
	void EnablePlugin_Test() {
		pluginManager.enablePlugin(plugin);
		assertTrue(plugin.isRunning());
	}

	@Test
	void shutdown_StopPlugins_Test() {
		AtomicBoolean pluginStopped = new AtomicBoolean(false);
		Plugin plugin = new Plugin("test",pluginManager,new ServerdPlugin() {
			@Override
			public String init(Plugin plugin) {
				return INIT_SUCCESS;
			}

			@Override
			public void work(Plugin plugin) {}

			@Override
			public void stop(Plugin plugin) {
				pluginStopped.set(true);
			}

			@Override
			public void metadata(Info info) {}
		});

		pluginManager.addPlugin(plugin);
		pluginManager.shutdown();

		assertTrue(pluginStopped.get());
	}
}

class PluginManagerTestPlugin implements ServerdPlugin {
	@Override
	public void metadata(Info info) {}

	@Override
	public String init(Plugin plugin) { return INIT_SUCCESS; }

	@Override
	public void work(Plugin plugin) {}

	@Override
	public void stop(Plugin plugin) {}	
}
