package com.serverd.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.serverd.client.ClientManager;
import com.serverd.client.TCPClient;
import com.serverd.config.Config;
import com.serverd.utils.Utils;

/**
 * TCP Server class.
 */
public class TCPServer extends Server {
	
	/** TCP Channel */
	protected AsynchronousServerSocketChannel tcpChannel;

	/**
	 * TCP Server constructor.
	 * @param ip Server IP.
	 * @param port Server port.
	 * @param config Global config.
	 */
	public TCPServer(String ip,int port,Config config) {
		super("TCP Server",ip,port,config);
		
		isEnabled = config.enableTcp;
	}

	@Override
	public void start() throws IOException {
		tcpChannel = AsynchronousServerSocketChannel.open();
		tcpChannel.bind(new InetSocketAddress(ip,port));
		
        tcpChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel clientSocketChannel, Void attachment) {
        		tcpChannel.accept(null, this);
            	acceptConnection(clientSocketChannel);
            }

            @Override
            public void failed(Throwable e, Void attachment) {
            	if (e instanceof AsynchronousCloseException)
            		return;
            	
            	log.error("Server error: " + e.getMessage());
            }
        });
        
    	//keep thread alive
    	while (isRunned()) 
    		Utils.sleep(1000);
	}

	/**
	 * Accepting connection.
	 * @param clientSocketChannel Client channel.
	 */
	protected void acceptConnection(AsynchronousSocketChannel clientSocketChannel) {
    	try {
    		log.info("Connection accepted from client!");
        	
        	TCPClient client = new TCPClient(ClientManager.getFreeClientID(),clientSocketChannel,config);
       	    client.setProcessor(getProcessorFactory().get(client));

        	ClientManager.setupClient(client);
        	ClientManager.addClient(client);
        	
        	client.setAfterReceive(() ->
					client.receive((bytes) ->
							client.getProcessor().receive(bytes)
					));
        	client.invokeReceive();
    		
    	} catch (IOException e) {
    		if (isRunned())
				log.error("Server error: " + e.getMessage());
    	}
	}

	@Override
	public void stop() throws IOException {		
		if (tcpChannel != null) {
			log.info("Stopping TCP server...");
			tcpChannel.close();
		}
	}
}
