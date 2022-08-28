package com.serverd.plugin.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

class CommandTest 
{

	@Test
	void checkArgs_Test()
	{	
		Command command = new Command() {	
			@Override
			public int checkArgs(String[] args,int length)
			{
				return super.checkArgs(args, length);
			}
			
			@Override
			public void execute(String[] args, Client client, Plugin plugin) {}
		};
		
		String[] args1 = {"Test","test","test","test"};
		String[] args2 = {"Test","test"};
		String[] args3 = {"Test","test","test","test","Test","test","test","test"};
		
		assertAll(
			() -> assertEquals(command.checkArgs(args1, 4), 0),
			() -> assertEquals(command.checkArgs(args2, 3), -1),
			() -> assertEquals(command.checkArgs(args3, 5), 1)
		);
	}

}