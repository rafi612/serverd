package com.serverd.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


/**
 * TCP client class
 */
public class TCPClient extends NonBlockingClient
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
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER);
			
		int len = tcpSocket.read(buffer);
		
		if (len == -1)
			throw new IOException("Connection closed");
			
		byte[] ret = new byte[len];
			
		System.arraycopy(buffer.array(), 0, ret, 0, len);
		
		return ret;
	}
	
	@Override
	public void rawdataSend(byte[] bytes) throws IOException
	{
		SelectionKey key = tcpSocket.keyFor(selector);
		key.interestOps(SelectionKey.OP_WRITE);
		
		queueBuffer(ByteBuffer.wrap(bytes));
	}
	
	@Override
	public void closeClient()
	{
		super.closeClient();
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
	public void processSend(ByteBuffer buffer) throws IOException {
		tcpSocket.write(buffer);
	}
}