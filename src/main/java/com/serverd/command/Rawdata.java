package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.client.ClientManager;
import com.serverd.plugin.Plugin;

public class Rawdata extends Command
{
	private int buffersize = 0,sended = 0;
	
	protected Rawdata()
	{
		command = "/rawdata";
		help = "/rawdata <buffer> - run rawdata mode with buffer";
	}
	@Override
	public void processReceive(byte[] buffer,Client client) throws IOException
	{
        Client joined = ClientManager.clients.get(client.getJoinedID());
		
		joined.rawdataSend(buffer);

		sended += buffer.length;
		
		if (sended >= buffersize)
		{
			client.send(ok());
			done();
		}
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		if (checkArgs(args,client, 1))
		{	
			if (client.isJoined())
			{
				client.send(ok());
				
				buffersize = Integer.parseInt(args[0]);
				
				client.log.info("Raw data mode started," + buffersize + " bytes can be sended");
				stayAlive();
			}
			else client.send(error("Not joined"));
		}
	}
}