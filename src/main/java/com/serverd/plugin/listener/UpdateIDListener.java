package com.serverd.plugin.listener;

import com.serverd.plugin.Plugin;

/**
 * Client's ID updating listener
 */
public interface UpdateIDListener
{
	/**
	 * Executing when client's id is swapped
	 * @param plugin Plugin instance
	 * @param oldid Old client id
	 * @param newid New client id
	 */
	public void updateID(Plugin plugin,int oldid,int newid);
}
