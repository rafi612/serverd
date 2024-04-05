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

/**
 * Implementing server for UDP protocol.
 */
public class UDPServer extends SelectableServer {
	
	/** UDP Channel*/
	public static DatagramChannel udpChannel;

	private int timeout;

	/**
	 * UDP Server default constructor.
	 * @param ip Server IP.
	 * @param port Server port.
	 * @param clientManager Client manager object.
	 * @param timeout Read timeout.
	 */
	public UDPServer(String ip,int port,ClientManager clientManager,int timeout) {
		super("UDP Server",ip,port,clientManager);

		this.timeout = timeout;
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
		while (isRunning()) {
			//timeout checking and selecting
			selectWithTimeout(selector, udpChannel,timeout);
			
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
							client.getProcessor().receive(client.receive());
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
					client.processQueue();
				}
			}
		}
		udpChannel.close();
	}

	protected void printOpenMessage(Client client) {
		log.info("Connection founded in " + client.getIP() + ":" + client.getPort());
	}
	
	/**
	 * Initializing UDP connection.
	 * @param channel Selectable channel instance.
	 * @param address Socket address.
	 * @param selector Selector instance.
	 * @param buffer First received buffer.
	 * @throws IOException when socket throws I/O error.
	 */
	protected void addConnection(DatagramChannel channel,InetSocketAddress address,Selector selector,ByteBuffer buffer) throws IOException {
		DatagramChannel dc = DatagramChannel.open();
		dc.configureBlocking(false);
		dc.socket().setReuseAddress(true);
		dc.bind(channel.socket().getLocalSocketAddress());
		dc.connect(address);
		
		UDPClient client = new UDPClient(clientManager.getFreeClientID(),clientManager, selector, dc, address);
		clientManager.addClient(client);
		
		dc.register(selector, SelectionKey.OP_READ,client);
		
		printOpenMessage(client);

		setupClientProcessor(client,getProcessorFactory().get(client));
		clientManager.setupClient(client);
		
		byte[] data = new byte[buffer.limit()];
		buffer.get(data, 0, buffer.limit());
		
		client.getProcessor().receive(data);
	}

	@Override
	public void stop() throws IOException {
		if (udpChannel != null) {
			log.info("Stopping UDP server...");
			udpChannel.close();
		}
	}

	/**
	 * Setting client timeout.
	 * @param timeout Client timeout.
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return Client timeout.
	 */
	public int getTimeout() {
		return timeout;
	}

}
