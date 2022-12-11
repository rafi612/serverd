package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

class CommandTest 
{
	@Test
	void checkArgsWithClient_Test()
	{	
		class TestClient extends Client
		{
			public TestClient(int id) 
			{
				super(id);
			}

			public int sended;

			@Override
			public void send(String message) throws IOException
			{
				sended++;
				super.send(message);
			}
		}
		TestClient client = new TestClient(0);
		
		Command command = new Command() {	
			@Override
			public int checkArgs(String[] args,Client client,int length) throws IOException
			{
				return super.checkArgs(args, client,length);
			}
			
			@Override
			public void execute(String[] args, Client client, Plugin plugin) {}
		};
		
		String[] args1 = {"Test","test","test","test"};
		String[] args2 = {"Test","test"};
		String[] args3 = {"Test","test","test","test","Test","test","test","test"};
		
		assertAll(
			() -> assertEquals(command.checkArgs(args1, client, 4), 0),
			() -> assertEquals(command.checkArgs(args2, client, 3), -1),
			() -> assertEquals(command.checkArgs(args3, client, 5), 1),
			() -> assertEquals(client.sended,2)
		);
	}

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