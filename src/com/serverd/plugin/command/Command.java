package com.serverd.plugin.command;

import com.serverd.client.Client;

public abstract class Command
{
	public String command = "",help = "";
	
	public Command()
	{
		
	}
	
	protected int checkArgs(String[] s,int l)
	{
		
		if (s.length < l) 
		{
			return -1;
		}
		else if (s.length > l)
		{
			return 1;
		}
		else return 0;
	}
	
	public abstract void execute(String[] args,Client client);
	

}
