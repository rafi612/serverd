package com.serverd.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

/**
 * Getting config property from environment.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface FromEnv {
	String value();
}
