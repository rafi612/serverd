package com.serverd.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.serverd.util.Util;

/**
 * UDP client class
 */
public class UDPClient extends Client
{
	DatagramSocket udp_sock;
	
	public InetAddress ip;
	public int port;

	/**
	 * UDPClient class constructor
	 * @param id Client's ID
	 * @param sock Datagram socket instance
	 * @param ip Client's IP
	 * @param port Client's port
	 * @throws UnknownHostException
	 */
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
	
	byte[] buffer = null;
	int bufferOffset = 0;
	int bufferLength = 0;
	
	@Override
	public String receive()
	{
		while (buffer == null)
			Util.sleep(1);
		
		String msg = new String(buffer,bufferOffset,bufferLength);
		buffer = null;
		
		msg = encoder.decode(msg, this);
		
		programlog.log(msg);
		return msg;
	}
	
	@Override
	public void send(String mess)
	{
		log.log(mess);
		
		String message = encoder.encode(mess, this); 
		
		DatagramPacket out = new DatagramPacket(message.getBytes(),message.length(),ip,port);
		try
		{
			udp_sock.send(out);
		} 
		catch (IOException e)
		{
			log.log("Send message failed: " + e.getMessage());
			crash(e);
		}
	}

	@Override
	public byte[] rawdata_receive(int buflen)
	{
		while (buffer == null)
			Util.sleep(1);
		
		int len = bufferLength;
		byte[] ret = new byte[len];
		
		System.arraycopy(buffer, 0, ret, 0, len);
		
		buffer = null;
		
		return ret;
	}
	
	@Override
	public void rawdata_send(byte[] b)
	{
		DatagramPacket p = new DatagramPacket(b, b.length, ip, port);
		try 
		{
			udp_sock.send(p);
		} 
		catch (IOException e) 
		{
			log.log("Rawdata send failed: " + e.getMessage());
			crash(e);
		}
	}
	
	@Override
	public String getIP()
	{
		return ip.getHostAddress();
	}
	
	@Override
	public int getPort()
	{
		return port;
	}
	
	@Override
	public void closeClient()
	{
		super.closeClient();
		
		udp_sock.close();
	}

}
