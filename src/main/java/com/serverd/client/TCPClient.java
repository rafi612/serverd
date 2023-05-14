package com.serverd.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * TCP client class
 */
public class TCPClient extends SelectableClient
{	
	/** Socket*/
	protected SocketChannel tcpSocket;
	
	/**
	 * TCPClient class constructor
	 * @param id Client's ID
	 * @param socket Socket instance
	 * @throws IOException when InputStream or OutputStream throws {@link IOException}
	 */
	public TCPClient(int id,Selector selector, SocketChannel socket) throws IOException
	{
		super(id,selector);
		
		protocol = Protocol.TCP;
		tcpSocket = socket;
	}

	
	@Override
	public void send(String mess) throws IOException
	{
		log.info("<Sended> " + mess);

		rawdataSend(encoder.encode(mess, this).getBytes());
	}
	
	@Override
	public byte[] rawdataReceive() throws IOException
	{		
		receiveBuffer.clear();
		int len = tcpSocket.read(receiveBuffer);
		receiveBuffer.flip();
		
		if (len == -1)
			throw new IOException("Connection closed");
			
		byte[] ret = new byte[len];
		receiveBuffer.get(ret, 0, len);
		
		updateTimeout();
		
		return ret;
	}
	
	@Override
	public void rawdataSend(byte[] bytes) throws IOException
	{
		getKey().interestOps(SelectionKey.OP_WRITE);
		
		queueBuffer(bytes);
		selector.wakeup();
	}
	
	@Override
	public void closeClient()
	{
		super.closeClient();
		
		try
		{
			tcpSocket.close();
		} 
		catch (IOException e)
		{
			log.error("Client closing failed: " + e.getMessage());
		}
	}
	
	@Override
	public SelectionKey getKey() {
		return tcpSocket.keyFor(selector);
	}
	
	@Override
	public String getIP()
	{
		return tcpSocket.socket().getInetAddress().getHostAddress();
	}
	
	@Override
	public int getPort()
	{
		return tcpSocket.socket().getPort();
	}


	@Override
	public long processSend(ByteBuffer buffer) throws IOException 
	{
		return tcpSocket.write(buffer);
	}
}
