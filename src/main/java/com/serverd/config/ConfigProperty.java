package com.serverd.config;

import java.lang.annotation.*;

/**
 * Config property declaration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigProperty {
	String value();
}
