package com.serverd.config;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Properties;

import com.serverd.main.Main;

/**
 * Default config instance and config loader and writer
 */
public class Config {

	@ConfigProperty("ip")
	public String ip = "0.0.0.0";
	
	@ConfigProperty("tcp.port")
	public int tcpPort = 9999;
	
	@ConfigProperty("udp.port")
	public int udpPort = 9998;

	@ConfigProperty("timeout")
	public int timeout = 0;
	
	@ConfigProperty("enable.tcp")
	public boolean enableTcp = true;
	@ConfigProperty("enable.udp")
	public boolean enableUdp = true;
	
	/**
	 * Loading config from file to given type, searching using {@link ConfigProperty} annotation
	 * @param <T> Type of returned config
	 * @param file Path to .properties file
	 * @param clazz Config class object
	 * @return Config object
	 * @throws IOException when IO error
	 */
	public static <T> T load(File file,Class<T> clazz) throws IOException {
		try (InputStream input = new FileInputStream(file)) {
			T config = clazz.getDeclaredConstructor().newInstance();
			Properties properties = new Properties();
			properties.load(input);
			
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(ConfigProperty.class)) {
					ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
					field.setAccessible(true);
					field.set(config, parseType(properties.getProperty(annotation.value()),field.getType()));
				}
			}
			return config;
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException  
				| InvocationTargetException | NoSuchMethodException  | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Saves config to given file
	 * @param file File to save
	 * @param config Config instance
	 * @param comment Comment
	 * @throws IOException when IO error
	 */
	public static void save(File file,Object config,String comment) throws IOException {
		try (OutputStream output = new FileOutputStream(file)) {
			Properties properties = new Properties();
			
			for (Field field : config.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(ConfigProperty.class)) {
					ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
					field.setAccessible(true);
					var objectValue = field.get(config);
					properties.setProperty(annotation.value(), Objects.toString(objectValue, "null"));
				}
			}
			properties.store(output, comment);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creating config when it does not exists
	 * @param file File to save
	 * @param config Config instance
	 * @param comment Comment
	 * @return if config exists before
	 * @throws IOException when IO error
	 */
	public static boolean createIfNotExists(File file,Object config,String comment) throws IOException {
		boolean before = !file.exists();
		if (before)
			save(file,config,comment);
		return !before;
	}
	
	/**
	 * Loading default server config
	 * @return Config instance
	 * @throws IOException when IO error
	 */
	public static Config loadDefault() throws IOException {
		return Config.load(new File(Main.workingdir,"config.properties"), Config.class);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T parseType(String input, Class<T> parseType) {
		if (parseType == String.class) {
			return (T) input;
		} else if (parseType == Integer.class || parseType == int.class) {
			return (T) Integer.valueOf(input);
		} else if (parseType == Long.class || parseType == long.class) {
			return (T) Long.valueOf(input);
		} else if (parseType == Float.class || parseType == float.class) {
			return (T) Float.valueOf(input);
		} else if (parseType == Double.class || parseType == double.class) {
			return (T) Double.valueOf(input);
		} else if (parseType == Boolean.class || parseType == boolean.class) {
			return (T) Boolean.valueOf(input);
		} else {
			throw new IllegalArgumentException("Unsupported type: " + parseType.getName());
		}
	}
}
