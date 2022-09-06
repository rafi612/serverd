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
	public Socket tcp_sock;
	
	public InputStream in;
	public OutputStream out;
	
	/**
	 * TCPClient class constructor
	 * @param id Client's ID
	 * @param s Socket instance
	 */
	public TCPClient(int id, Socket s)
	{
		super(id);
		
		protocol = Protocol.TCP;
		
		tcp_sock = s;
		try 
		{
			in = tcp_sock.getInputStream();
			out = tcp_sock.getOutputStream();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		thread = new Thread(this,"Client " + id);
	}
	
	@Override
	public String receive() throws IOException
	{
		byte[] buffer = new byte[BUFFER];
		int len = in.read(buffer);
			
		if (len == -1)
			throw new IOException("Connection closed");
		
		String message = encoder.decode(new String(buffer,0,len), this);
		
		if (!message.equals(""))
			programlog.info("<Reveived> " + message);
		
		return message;
	}
	
	@Override
	public void send(String mess) throws IOException
	{
		log.info("<Sended> " + mess);

		out.write(encoder.encode(mess, this).getBytes());
		out.flush();
	}
	
	@Override
	public byte[] rawdata_receive(int buflen) throws IOException
	{
		byte[] buffer = new byte[buflen];
		byte[] ret = null;

		int len = in.read(buffer);
			
		if (len == -1)
			throw new IOException("Connection closed");
			
		ret = new byte[len];
			
		System.arraycopy(buffer, 0, ret, 0, len);
		
		return ret;
	}
	
	@Override
	public void rawdata_send(byte[] b) throws IOException
	{
		out.write(b);
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
			tcp_sock.close();
		} 
		catch (IOException e)
		{
			log.error("Client closing failed: " + e.getMessage());
		}
	}
	
	@Override
	public String getIP()
	{
		return tcp_sock.getInetAddress().getHostAddress();
	}
	
	@Override
	public int getPort()
	{
		return tcp_sock.getPort();
	}
}