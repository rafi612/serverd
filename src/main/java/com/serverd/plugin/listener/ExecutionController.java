package com.serverd.plugin.listener;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

public interface ExecutionController
{
	/**
	 * Controls whether the command can be executed
	 * @param command Executed command
	 * @param client Client instance
	 * @param plugin Plugin instance
	 * @return whether the command should execute
	 */
	public boolean controlCommand(String command,String[] args,Client client,Plugin plugin) throws IOException;
}
