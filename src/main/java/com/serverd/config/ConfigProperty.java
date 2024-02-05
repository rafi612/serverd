package com.serverd.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

/**dd
 * ConfigProperty is used to bind variable to value stored in the configuration file.
 * Supported variable types are:
 * <ul>
 * <li>{@link String}</li>
 * <li>{@link Integer}</li>
 * <li>{@link Long}</li>
 * <li>{@link Float}</li>
 * <li>{@link Double}</li>
 * <li>{@link Boolean}</li>
 * </ul>
 * Types are automatically parsed by {@link Config#load} function.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ConfigProperty {
	String value();
}
