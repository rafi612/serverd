package com.serverd.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UDP client class
 */
public class UDPClient extends Client
{
	DatagramSocket udp_sock;
	DatagramPacket firstPacket;
	
	public InetAddress ip;
	public int port;

	/**
	 * UDPClient class constructor
	 * @param id Client's ID
	 * @param sock Datagram socket instance
	 * @param firstPacket first packet received
	 * @param ip Client's IP
	 * @param port Client's port
	 * @throws IOException
	 */
	public UDPClient(int id,DatagramSocket sock,DatagramPacket firstPacket,InetAddress ip,int port) throws IOException
	{
		super(id);
		
		protocol = Protocol.UDP;
		
		udp_sock = new DatagramSocket(null);
		udp_sock.setReuseAddress(true);
		udp_sock.bind(sock.getLocalSocketAddress());
		udp_sock.connect(ip,port);
		
		this.ip = ip;
		this.port = port;
		this.firstPacket = firstPacket;
		
		thread = new Thread(this, "UDP Client " + id);
	}
	
	@Override
	public synchronized String receive() throws IOException
	{
		byte[] buffer = new byte[Client.BUFFER];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		if (firstPacket == null)
			udp_sock.receive(packet);
		else
			packet = firstPacket;
		
		String msg = new String(packet.getData(),packet.getOffset(),packet.getLength());
		
		firstPacket = null;
		
		msg = encoder.decode(msg, this);
		
		programlog.info("<Received> " + msg);
		return msg;
	}
	
	@Override
	public void send(String mess) throws IOException
	{
		log.info("<Sended> " + mess);
		
		String message = encoder.encode(mess, this); 
		
		DatagramPacket out = new DatagramPacket(message.getBytes(),message.length(),ip,port);
		
		udp_sock.send(out);
	}

	@Override
	public synchronized byte[] rawdata_receive(int buflen) throws IOException
	{
		byte[] buffer = new byte[Client.BUFFER];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		if (firstPacket == null)
			udp_sock.receive(packet);
		else
			packet = firstPacket;
		
		byte[] ret = new byte[buffer.length];
		
		System.arraycopy(buffer, 0, ret, 0, buffer.length);
		
		firstPacket = null;
		
		return ret;
	}
	
	@Override
	public void rawdata_send(byte[] b) throws IOException
	{
		DatagramPacket p = new DatagramPacket(b, b.length, ip, port);
		
		udp_sock.send(p);
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
		udp_sock.disconnect();
		udp_sock.close();
	}

}
