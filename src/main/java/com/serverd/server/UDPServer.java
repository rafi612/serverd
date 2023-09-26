package com.serverd.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import com.serverd.client.Client;
import com.serverd.client.ClientManager;
import com.serverd.client.UDPClient;
import com.serverd.config.Config;

/**
 * UDP Server class
 */
public class UDPServer extends SelectableServer {
	
	/** UDP Channel*/
	public static DatagramChannel udpChannel;

	/**
	 * UDP Server default constructor
	 * @param ip Server IP
	 * @param port Server port
	 * @param config Global config
	 */
	public UDPServer(String ip,int port,Config config) {
		super("UDP Server",ip,port,config);

		isEnabled = config.enableUdp;
	}

	@Override
	public void start() throws IOException {
		udpChannel = DatagramChannel.open();
		udpChannel.configureBlocking(false);
		udpChannel.socket().setReuseAddress(true);
		udpChannel.bind(new InetSocketAddress(ip,port));
		
		Selector selector = Selector.open();
		udpChannel.register(selector, SelectionKey.OP_READ);
		
		ByteBuffer buffer = ByteBuffer.allocate(Client.BUFFER);
		while (isRunned())
		{
			//timeout checking and selecting
			selectWithTimeout(selector, udpChannel,config.timeout);
			
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey key = keys.next();
				keys.remove();
				
				if (!key.isValid())
					continue;
				
				if (key.isReadable()) {
					//new client
					if (key.attachment() == null) {
						buffer.clear();
						DatagramChannel channel = (DatagramChannel) key.channel();
						InetSocketAddress address = (InetSocketAddress) channel.receive(buffer);
						buffer.flip();
						
						addConnection(channel, address, selector, buffer);
					} else {
						UDPClient client = (UDPClient) key.attachment();
						try {
							client.getProcessor().receive(client.rawdataReceive());
						} catch (IOException e) {
							client.crash(e);
							continue;
						}
					}
				}
				
				if (!key.isValid())
					continue;
				
				if (key.isWritable()) {
					UDPClient client = (UDPClient) key.attachment();
					
					if (client.processQueue()) {
						key.interestOps(SelectionKey.OP_READ);
						if (client.isJoined())
							client.getJoiner().unlockRead();
					}
					key.interestOps(SelectionKey.OP_READ);
				}
			}
		}
		udpChannel.close();
	}
	
	public void addConnection(DatagramChannel channel,InetSocketAddress address,Selector selector,ByteBuffer buffer) throws IOException {
		DatagramChannel dc = DatagramChannel.open();
		dc.configureBlocking(false);
		dc.socket().setReuseAddress(true);
		dc.bind(channel.socket().getLocalSocketAddress());
		dc.connect(address);
		
		UDPClient client = new UDPClient(ClientManager.getFreeClientID(), selector, dc, address);
		ClientManager.addClient(client);
		
		dc.register(selector, SelectionKey.OP_READ,client);
		
		log.info("Connection founded in " + client.getIP() + ":" + client.getPort());	
		
		ClientManager.setupClient(client);
		
		byte[] data = new byte[buffer.limit()];
		buffer.get(data, 0, buffer.limit());
		
		client.getProcessor().receive(data);
	}

	@Override
	public void stop() throws IOException {
		if (udpChannel != null) {
			log.info("Stopping UDP server..");
			udpChannel.close();
		}
	}

}
