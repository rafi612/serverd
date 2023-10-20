package com.serverd.client.processor;

import com.serverd.client.Client;

/**
 * Processor Factory interface.
 */
@FunctionalInterface
public interface ProcessorFactory {
	Processor get(Client client);
}
