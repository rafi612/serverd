package com.serverd.plugin.listener;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

/**
 * Interface to control execution of commands in clients
 */
public interface ExecutionController
{
	/**
	 * Controls whether the command can be executed
	 * @param command Executed command
	 * @param args Command arguments
	 * @param client Client instance
	 * @param plugin Plugin instance
	 * @return whether the command should execute
	 * @throws IOException when client throw {@link IOException}
	 */
	public boolean controlCommand(String command,String[] args,Client client,Plugin plugin) throws IOException;
}
