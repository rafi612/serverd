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
	//tcp
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
		thread.start();
	}
	
	@Override
	public String receive()
	{
		String message = "";
		try 
		{
			byte[] buffer = new byte[BUFFER];
			int len = in.read(buffer);
			
			if (len == -1)
				throw new IOException("Connection closed");

			message = new String(buffer,0,len);
		} 
		catch (Exception e)
		{
			log.error("Receive message failed: " + e.getMessage());
			crash(e);
		}
		
		message = encoder.decode(message, this);
		
		if (!message.equals(""))
			programlog.info("<Reveived> " + message);
		
		return message;
	}
	
	@Override
	public void send(String mess)
	{
		log.info("<Sended> " + mess);
		try 
		{
			out.write(encoder.encode(mess, this).getBytes());
			out.flush();
		} 
		catch (Exception e) 
		{
			log.error("Send message failed: " + e.getMessage());
			crash(e);
		}
	}
	
	@Override
	public byte[] rawdata_receive(int buflen)
	{
		byte[] buffer = new byte[buflen];
		byte[] ret = null;
		
		try 
		{
			int len = in.read(buffer);
			
			if (len == -1)
				throw new IOException("Connection closed");
			
			ret = new byte[len];
			
			System.arraycopy(buffer, 0, ret, 0, len);
			
		} 
		catch (IOException e)
		{
			log.error("Rawdata receive failed: " + e.getMessage());
			crash(e);
		}
		
		
		return ret;
		
	}
	
	@Override
	public void rawdata_send(byte[] b)
	{
		try 
		{
			out.write(b);
			out.flush();
		}
		catch (IOException e)
		{
			log.error("Rawdata send failed: " + e.getMessage());
			crash(e);
		}
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