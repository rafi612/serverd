package com.serverd.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.serverd.log.Log;

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
			// TODO Auto-generated catch block
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

			for (int i = 0; i < len;i++)
				if(buffer[i] != 0)
					message += Character.toString((char)buffer[i]);
		} 
		catch (Exception e)
		{
			//crash(e);
			Log.log("ClientThread " + id,"Receive message failed");
		}
		
		if (!message.equals(""))
			Log.log("Client Program " + id,message);
		
		return message;
	}
	
	public void send(String mess)
	{
		Log.log("ClientThread " + id,mess);
		try 
		{
			out.write(mess.getBytes());
			out.flush();
		} 
		catch (Exception e) 
		{
			//crash(e);
			Log.log("ClientThread " + id,"Send message failed");
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
			
			for (int i = 0;i < len;i++)
				ret[i] = buffer[i];
			
		} 
		catch (IOException e)
		{
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
			// TODO Auto-generated catch block
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
