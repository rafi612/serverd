package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.client.ClientManager;
import com.serverd.plugin.Plugin;

public class Rawdata extends Command
{
	protected Rawdata()
	{
		command = "/rawdata";
		help = "/rawdata <buffer> - run rawdata mode with buffer";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException 
	{
		if (checkArgs(args,client, 1) == 0)
		{	
			int buffersize = Integer.parseInt(args[0]);

            Client joined = ClientManager.clients.get(client.getJoinedID());
			
			if (client.isJoined())
			{
				client.send(ok());
				
				client.log.info("Raw data mode started," + buffersize + " bytes can be sended");
				int i = 0;
				while (i < buffersize)
				{
					byte[] buffer = client.rawdataReceive();
					joined.rawdataSend(buffer);

					i+= buffer.length;
				}
				client.send(ok());
			}
			else client.send(error("Not joined"));
		}
	}
}