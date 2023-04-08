package com.serverd.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


/**
 * TCP client class
 */
public class TCPClient extends Client
{	
	/** Socket*/
	protected Socket tcpSocket;
	
	/** Input stream*/
	protected InputStream in;
	/** Output stream*/
	protected OutputStream out;
	
	/**
	 * TCPClient class constructor
	 * @param id Client's ID
	 * @param socket Socket instance
	 * @throws IOException when InputStream or OutputStream throws {@link IOException}
	 */
	public TCPClient(int id, Socket socket) throws IOException
	{
		super(id);
		
		protocol = Protocol.TCP;
		
		tcpSocket = socket;
		
		in = tcpSocket.getInputStream();
		out = tcpSocket.getOutputStream();
			
		thread = new Thread(this,"Client " + id);
	}

	
	@Override
	public void send(String mess) throws IOException
	{
		log.info("<Sended> " + mess);

		out.write(encoder.encode(mess, this).getBytes());
		out.flush();
	}
	
	@Override
	public byte[] receive() throws IOException
	{
		byte[] buffer = new byte[BUFFER];

		int len = in.read(buffer);
			
		if (len == -1)
			throw new IOException("Connection closed");
			
		byte[] ret = new byte[len];
			
		System.arraycopy(buffer, 0, ret, 0, len);
		
		return ret;
	}
	
	@Override
	public void rawdataSend(byte[] bytes) throws IOException
	{
		out.write(bytes);
		out.flush();
	}
	
	@Override
	public void closeClient()
	{
		super.closeClient();
		
		try
		{
			in.close();
			out.close();
			tcpSocket.close();
		} 
		catch (IOException e)
		{
			log.error("Client closing failed: " + e.getMessage());
		}
	}
	
	@Override
	public String getIP()
	{
		return tcpSocket.getInetAddress().getHostAddress();
	}
	
	@Override
	public int getPort()
	{
		return tcpSocket.getPort();
	}
}