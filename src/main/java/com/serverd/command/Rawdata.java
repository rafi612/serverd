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
		CommandProcessor processor = (CommandProcessor) client.getProcessor();
		if (checkArgs(args,client, 1)) {	
			if (processor.isJoined()) {
				send(client,ok(),() -> {
					int bufferSize = Integer.parseInt(args[0]);
					
					client.log().info("Raw data mode started," + bufferSize + " bytes can be sended");
					
					Client joined = processor.getJoiner();
					
					ReceiveContinuation receive = new ReceiveContinuation() {
						@Override
						public void receiveDone(byte[] bytes) throws IOException {
							send(joined,bytes,() -> {
								sended += bytes.length;
								
								if (sended >= bufferSize)
									send(client,ok());
								else 
									receive(this);
							});
							
						}
					};
					receive(receive);
				});
			}
			else send(client,error("Not joined"));
		}
	}
}
