package com.serverd.client.processor;

import com.serverd.client.Client;

/**
 * Processor Factory interface.
 * Processor Factory is used to create new instances of {@link Processor}.
 */
@FunctionalInterface
public interface ProcessorFactory {
	/**
	 * Produces {@link Processor} instance.
	 */
	Processor get(Client client);
}
