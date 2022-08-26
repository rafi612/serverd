package com.serverd.plugin.listener;

import java.util.List;

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
	
	/**
	 * Updates specific list with ID of clients
	 * @param list List object to update
	 * @param oldid Old id of client
	 * @param newid New id of client
	 */
	default void updateIDInList(List<Integer> list,int oldid,int newid)
	{
		if (oldid == newid) return;
		
        if (list.size() > 0)
        {
            int index = list.lastIndexOf(oldid);
            if (index != -1)
                list.set(index, newid);
        }
	}
}
