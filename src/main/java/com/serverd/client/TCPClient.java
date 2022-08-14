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
	
	public String receive()
	{
		String message = "";
		try 
		{
			byte[] buffer = new byte[BUFFER];
			int len = in.read(buffer);

			message = new String(buffer,0,len);
		} 
		catch (Exception e)
		{
			log.log("Receive message failed: " + e.getMessage());
		}
		
		message = encoder.decode(message, this);
		
		if (!message.equals(""))
			programlog.log(message);
		
		return message;
	}
	
	public void send(String mess)
	{
		log.log(mess);
		try 
		{
			out.write(encoder.encode(mess, this).getBytes());
			out.flush();
		} 
		catch (Exception e) 
		{
			//crash(e);
			log.log("Send message failed: " + e.getMessage());
		}
	}
	
	public byte[] rawdata_receive(int buflen)
	{
		byte[] buffer = new byte[buflen];
		byte[] ret = null;
		
		try 
		{
			int len = in.read(buffer);
			ret = new byte[len];
			
			System.arraycopy(buffer, 0, ret, 0, len);
			
		} 
		catch (IOException e)
		{
			log.log("Rawdata receive failed: " + e.getMessage());
			crash(e);
		}
		
		
		return ret;
		
	}
	
	public void rawdata_send(byte[] b)
	{
		try 
		{
			out.write(b);
			out.flush();
		}
		catch (IOException e)
		{
			log.log("Rawdata send failed: " + e.getMessage());
			crash(e);
		}
	}
	
	public void closeSocket() 
	{
		connected = false;
		
		try {
			in.close();
			out.close();
			tcp_sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeClient()
	{
		closeSocket();
	}
	
	public String getIP()
	{
		return tcp_sock.getInetAddress().getHostAddress();
	}
	
	public int getPort()
	{
		return tcp_sock.getPort();
	}
}