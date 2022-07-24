package com.serverd.plugin.listener;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

public interface ExecutionController
{
	/**
	 * Controls whether the command can be executed
	 * @param command Executed command
	 * @param client Client instance
	 * @param plugin Plugin instance
	 * @return Error message, if null or "" then command can be executed
	 */
	public String controlCommand(String command,String[] args,Client client,Plugin plugin);
}
