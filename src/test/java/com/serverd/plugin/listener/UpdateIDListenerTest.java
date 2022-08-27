package com.serverd.plugin.listener;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.serverd.plugin.Plugin;

class UpdateIDListenerTest
{
	private class TestInterface implements UpdateIDListener
	{
		@Override
		public void updateID(Plugin plugin, int oldid, int newid) {}
	}

	@Test
	void updateIDInList_Test() 
	{
		TestInterface listener = new TestInterface();
		
		ArrayList<Integer> clientID = new ArrayList<>();
		
		for (int i = 0; i < 10;i++)
			clientID.add(i);
		
		listener.updateIDInList(clientID, 1, 0);
		assertEquals(clientID.get(1),0);
		
		listener.updateIDInList(clientID, 9, 8);
		assertEquals(clientID.get(9),8);
	}

}
