package com.serverd.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import com.serverd.main.Main;

public class Config {

	@ConfigProperty("ip")
	public String ip = "0.0.0.0";
	
	@ConfigProperty("tcp.port")
	public int tcpPort = 9999;
	
	@ConfigProperty("udp.port")
	public int udpPort = 9998;

	@ConfigProperty("timeout")
	public int timeout = 5 * 60 * 1000;
	
	@ConfigProperty("enable.tcp")
	public boolean enableTcp = true;
	@ConfigProperty("enable.udp")
	public boolean enableUdp = true;
	
	public static <T> T load(File path,Class<T> clazz) throws IOException {
		try (InputStream input = new FileInputStream(path)) {
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
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void save(File path,Object config,String comment) throws IOException, IllegalArgumentException, IllegalAccessException {
		try (OutputStream output = new FileOutputStream(path)) {
			Properties properties = new Properties();
			
			for (Field field : config.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(ConfigProperty.class)) {
					ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
					
					properties.setProperty(annotation.value(), field.get(config).toString());
				}
			}
			properties.store(output, comment);
		}
	}
	
	public static void createIfNotExists(File path,Object config,String comment) throws IllegalArgumentException, IllegalAccessException, IOException {
		if (!path.exists())
			save(path,config,comment);
	}
	
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
