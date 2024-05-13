package com.serverd.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.serverd.app.ServerdApplication;

class ConfigTest {

    private static final String TEST_FILE_NAME = "test.properties";
    
    @TempDir
    static File tempDir;
    
    private String testFilePath;
    
    private TestConfig testConfig;
    
    @BeforeEach
    public void setUp() throws IOException {
    	testConfig = new TestConfig();
        testConfig.stringProperty = "test string";
        testConfig.intProperty = 123;
    	
        // Create a test properties file with some test data in the temporary directory
        Properties properties = new Properties();
        properties.setProperty("test.string.property", "Test string value");
        properties.setProperty("test.int.property", "123");
        testFilePath = new File(tempDir,TEST_FILE_NAME).getPath();
        try (FileOutputStream out = new FileOutputStream(testFilePath)) {
            properties.store(out, null);
        }
    }
    
    @AfterEach
    public void cleanUp() throws IOException {
        // Delete the test properties file from the temporary directory
        Path path = Paths.get(testFilePath);
        Files.deleteIfExists(path);
    }
    
    @Test
    public void load_loadStringProperty_Test() throws IOException {
        // Test loading a string property from the test properties file
        File file = new File(testFilePath);
        TestConfig config = Config.load(file, TestConfig.class);
        assertNotNull(config);
        assertEquals("Test string value", config.stringProperty);
    }
    
    @Test
    public void load_loadIntProperty_Test() throws IOException {
        // Test loading an integer property from the test properties file
        File file = new File(testFilePath);
        TestConfig config = Config.load(file, TestConfig.class);
        assertNotNull(config);
        assertEquals(123, config.intProperty);
    }
    

    @Test
    void save_Test() throws IOException {
        // Arrange
        String comment = "test save method";
        File outputFile = new File(tempDir,"output.properties");

        // Act
        Config.save(outputFile, testConfig, comment);

        // Assert
        assertTrue(outputFile.exists());
        assertEquals("#" + comment, Files.lines(outputFile.toPath()).findFirst().orElse(""));
        assertEquals(testConfig.stringProperty, Files.lines(outputFile.toPath())
                .filter(line -> line.contains("test.string.property"))
                .map(line -> line.substring(line.indexOf("=") + 1)).findFirst().orElse(""));
        assertEquals(String.valueOf(testConfig.intProperty), Files.lines(outputFile.toPath())
                .filter(line -> line.contains("test.int.property"))
                .map(line -> line.substring(line.indexOf("=") + 1)).findFirst().orElse(""));
    }

    @Test
    void save_saveWithNullValues_Test() throws IOException {
        // Arrange
        String comment = "test save method with null values";
        testConfig.stringProperty = null;
        testConfig.intProperty = 0;
        File outputFile = new File(tempDir,"output.properties");

        // Act
        Config.save(outputFile, testConfig, comment);

        // Assert
        assertTrue(outputFile.exists());
        assertEquals("#" + comment, Files.lines(outputFile.toPath()).findFirst().orElse(""));
        assertTrue(Files.lines(outputFile.toPath()).anyMatch(line -> line.contains("test.string.property=null")));
        assertTrue(Files.lines(outputFile.toPath()).anyMatch(line -> line.contains("test.int.property=0")));
    }
    
    @Test
    void createIfNotExists_CreatesFileWhenNotExists_Test() throws IOException {
        // Arrange
        String comment = "Test comment";
        testConfig.stringProperty = "test string value";
        testConfig.intProperty = 123;
        File outputFile = new File(tempDir,"output.properties");

        // Act
        boolean result = Config.createIfNotExists(outputFile, testConfig, comment);

        // Assert
        assertTrue(outputFile.exists());
        assertTrue(result);
    }

    @Test
    void createIfNotExists_DoesNotCreateFileWhenExists_Test() throws IOException {
        // Arrange
        String comment = "Test comment";
        testConfig.stringProperty = "test string value";
        testConfig.intProperty = 123;
        File outputFile = new File(tempDir,"test-config" + new Random().nextInt() + ".properties");

        // Create file before running the test
        if (!outputFile.createNewFile())
            fail("Output file not created");

        assertFalse(Config.createIfNotExists(outputFile, testConfig, comment));
    }

    @Test
    void createIfNotExists_SavesValuesToFileWhenNotExists_Test() throws IOException {
        // Arrange
        String comment = "Test comment";
        testConfig.stringProperty = "test string value";
        testConfig.intProperty = 123;
        File outputFile = new File(tempDir,"test-config" + new Random().nextInt() + ".properties");

        // Act
        Config.createIfNotExists(outputFile, testConfig, comment);

        // Assert
        assertTrue(outputFile.exists());
        assertTrue(Files.lines(outputFile.toPath()).anyMatch(line -> line.contains("test.string.property=test string value")));
        assertTrue(Files.lines(outputFile.toPath()).anyMatch(line -> line.contains("test.int.property=123")));
    }

    @Test
    void createIfNotExists_DoesNotModifyValuesWhenExists_Test() throws IOException {
        // Arrange
        String comment = "Test comment";
        testConfig.stringProperty = "test string value";
        testConfig.intProperty = 123;
        File outputFile = new File(tempDir,"output.properties");

        // Create file before running the test with some initial content
        Files.write(outputFile.toPath(), List.of("initial line"), StandardCharsets.UTF_8);

        // Act
        Config.createIfNotExists(outputFile, testConfig, comment);

        // Assert
        assertTrue(outputFile.exists());
        assertTrue(Files.lines(outputFile.toPath()).anyMatch(line -> line.contains("initial line")));
        assertFalse(Files.lines(outputFile.toPath()).anyMatch(line -> line.contains("test.string.property=test string value")));
        assertFalse(Files.lines(outputFile.toPath()).anyMatch(line -> line.contains("test.int.property=123")));
    }
    
    @Test
    void loadDefault_Test() throws IOException {
        // Arrange
        Config expectedConfig = new Config();
        
        // Act
        ServerdApplication app = new ServerdApplication();
        app.setWorkdir(tempDir.getAbsoluteFile());
        Config.save(new File(app.getWorkdir().getAbsolutePath(),"config.properties"), expectedConfig, "test");
        Config actualConfig = Config.loadDefault(app);
        
        // Assert
        assertNotNull(actualConfig);
        assertEquals(expectedConfig.ip, actualConfig.ip);
        assertEquals(expectedConfig.tcpPort, actualConfig.tcpPort);
        assertEquals(expectedConfig.udpPort, actualConfig.udpPort);
        assertEquals(expectedConfig.timeout, actualConfig.timeout);
        assertEquals(expectedConfig.enableTcp, actualConfig.enableTcp);
        assertEquals(expectedConfig.enableUdp, actualConfig.enableUdp);
    }
    
    protected static class TestConfig {
        @ConfigProperty("test.string.property")
        public String stringProperty;
        
        @ConfigProperty("test.int.property")
        public int intProperty;
    }
}
