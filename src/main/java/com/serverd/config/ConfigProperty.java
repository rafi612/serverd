package com.serverd.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

/**
 * Config property declaration.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ConfigProperty {
	String value();
}
