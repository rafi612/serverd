package com.serverd.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * UDP client class
 */
public class UDPClient extends SelectableClient
{
	/** UDP Socket*/
	protected DatagramChannel udpSocket;
	
	/** IP*/
	protected InetSocketAddress address;

	/**
	 * UDPClient class constructor
	 * @param id Client's ID
	 * @param selector Selector instance
	 * @param udpSocket Datagram socket instance
	 * @param address Client's address
	 * @throws IOException
	 */
	public UDPClient(int id,Selector selector,DatagramChannel udpSocket,SocketAddress address) throws IOException
	{
		super(id,selector);
		
		this.udpSocket = udpSocket;
		this.address = (InetSocketAddress) address;
		
		protocol = Protocol.UDP;
		
	}
	
	@Override
	public void send(String mess) throws IOException
	{
		log.info("<Sended> " + mess);

		rawdataSend(encoder.encode(mess, this).getBytes());
	}
	
	@Override
	public void rawdataSend(byte[] bytes) throws IOException
	{
		SelectionKey key = udpSocket.keyFor(selector);
		key.interestOps(SelectionKey.OP_WRITE);
		
		queueBuffer(bytes);
		selector.wakeup();
	}
	
	@Override
	public SelectionKey getKey() {
		return udpSocket.keyFor(selector);
	}
	
	@Override
	public String getIP()
	{
		return address.getAddress().getHostAddress();
	}
	
	@Override
	public int getPort()
	{
		return address.getPort();
	}
	
	@Override
	public void closeClient()
	{
		super.closeClient();
	}

	@Override
	public long processSend(ByteBuffer buffer) throws IOException 
	{
		udpSocket.send(buffer,address);
		return (long) buffer.position();
	}
}
