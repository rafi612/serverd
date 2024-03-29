package com.serverd.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogTest {
	Log logger;

	@BeforeEach
	void setUp() {
		logger = new Log("Test");
	}

	@Test
	void info_Test() {
		logger.info("Test info message");
	}
	
	@Test
	void warn_Test() {
		logger.warn("Test warning message");
	}
	
	@Test
	void error_Test() {
		logger.error("Test error message");
	}

	@Test
	void debug_Test() {
		logger.debug("Test debug message");
	}
	
	@Test
	void trace_Test() {
		logger.trace("Test trace message");
	}
}