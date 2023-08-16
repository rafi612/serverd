package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.plugin.Plugin;

public class Rawdata extends Command {
	private int sended = 0;
	
	protected Rawdata() {
		command = "/rawdata";
		help = "/rawdata <buffer> - run rawdata mode with buffer";
	}
	
	@Override
	public void execute(String[] args, Client client, Plugin plugin) throws IOException {
		if (checkArgs(args,client, 1)) {	
			if (client.isJoined()) {
				send(client,ok(),() -> {
					int buffersize = Integer.parseInt(args[0]);
					
					client.log.info("Raw data mode started," + buffersize + " bytes can be sended");
					
					Client joined = client.getJoiner();
					
					ReceiveContinuation receive = new ReceiveContinuation() {
						@Override
						public void receiveDone(byte[] bytes) throws IOException {
							send(joined,bytes,() -> {
								sended += bytes.length;
								
								if (sended >= buffersize)
									send(client,ok());
								else 
									receive(client,this);
							});
							
						}
					};
					receive(client,receive);
				});
			}
			else send(client,error("Not joined"));
		}
	}
}
