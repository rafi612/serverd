package com.serverd.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.serverd.log.Log;
import com.serverd.util.Util;

public class UDPClient extends Client
{
	DatagramSocket udp_sock;
	
	public InetAddress ip;
	public int port;

	public UDPClient(int id,DatagramSocket sock,InetAddress ip,int port) throws UnknownHostException
	{
		super(id);
		
		protocol = Protocol.UDP;
		
		udp_sock = sock;
		
		this.ip = ip;
		this.port = port;
		
		thread = new Thread(this, "UDP Client " + id);
		thread.start();
	}
	
	DatagramPacket buffer = null;
	
	public String receive()
	{
		while (buffer == null)
			Util.sleep(1);
		String msg = new String(buffer.getData(),buffer.getOffset(),buffer.getLength());
		buffer = null;
		Log.log("Client Program " + id,msg);
		return msg;
	}
	
	public void send(String message)
	{
		Log.log("ClientThread " + id,message);
		DatagramPacket out = new DatagramPacket(message.getBytes(),message.length(),ip,port);
		try
		{
			udp_sock.send(out);
		} 
		catch (IOException e)
		{
			crash(e);
		}
	}
	
	public byte[] rawdata_receive(int buflen)
	{
		while (buffer == null)
			Util.sleep(1);
		int len = buffer.getLength();
		byte[] ret = new byte[len];
		byte[] buf = buffer.getData();
		
		buffer = null;
		
		for (int i = 0;i < len;i++)
			ret[i] = buf[i];
		
		return ret;
	}
	
	public void rawdata_send(byte[] b)
	{
		DatagramPacket p = new DatagramPacket(b, b.length, ip, port);
		try 
		{
			udp_sock.send(p);
		} 
		catch (IOException e) 
		{
			crash(e);
		}
	}
	
	public String getIP()
	{
		return ip.getHostAddress();
	}
	
	public int getPort()
	{
		return port;
	}
	
	public void closeClient()
	{
		
	}

}
