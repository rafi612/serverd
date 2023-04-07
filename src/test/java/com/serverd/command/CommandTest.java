package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

class CommandTest 
{
	@Test
	void checkArgsWithClient_Test()
	{	
		TestClient client = new TestClient();
		
		Command command = new Command() {
			@Override
			public void execute(String[] args, Client client, Plugin plugin) {}
		};
		
		String[] args1 = {"Test","test","test","test"};
		String[] args2 = {"Test","test"};
		String[] args3 = {"Test","test","test","test","Test","test","test","test"};
		
		assertAll(
			() -> assertTrue(command.checkArgs(args1, client, 4)),
			() -> assertFalse(command.checkArgs(args2, client, 3)),
			() -> assertFalse(command.checkArgs(args3, client, 5)),
			() -> assertEquals(client.getSend().length,2)
		);
	}

	@Test
	void checkArgs_Test()
	{	
		Command command = new Command() {			
			@Override
			public void execute(String[] args, Client client, Plugin plugin) {}
		};
		
		String[] args1 = {"Test","test","test","test"};
		String[] args2 = {"Test","test"};
		String[] args3 = {"Test","test","test","test","Test","test","test","test"};
		
		assertAll(
			() -> assertTrue(command.checkArgs(args1, 4)),
			() -> assertTrue(command.checkArgs(args2, 3, Command.ARGS_LESS)),
			() -> assertTrue(command.checkArgs(args3, 5,Command.ARGS_MORE))
		);
	}
}
