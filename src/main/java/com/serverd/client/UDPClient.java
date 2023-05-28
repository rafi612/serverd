package com.serverd.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.serverd.config.Config;

/**
 * UDP client class
 */
public class UDPClient extends Client {
	/** UDP Socket*/
	protected DatagramSocket udpSocket;
	/** First package*/
	protected DatagramPacket firstPacket;
	
	/** IP*/
	protected InetAddress ip;
	/** Port*/
	protected int port;

	/**
	 * UDPClient class constructor
	 * @param id Client's ID
	 * @param sock Datagram socket instance
	 * @param firstPacket first packet received
	 * @param ip Client's IP
	 * @param port Client's port
	 * @throws IOException if socket throw error
	 */
	public UDPClient(int id,DatagramSocket sock,DatagramPacket firstPacket,InetAddress ip,int port,Config config) throws IOException {
		super(id);
		
		this.ip = ip;
		this.port = port;
		this.firstPacket = firstPacket;
		
		protocol = Protocol.UDP;
		
		udpSocket = new DatagramSocket(null);
		udpSocket.setReuseAddress(true);
		udpSocket.bind(sock.getLocalSocketAddress());
		udpSocket.connect(ip,port);
		
		udpSocket.setSoTimeout(config.timeout);
		
		thread = new Thread(this, "UDP Client " + id);
	}
	
	
	@Override
	public void send(String mess) throws IOException {
		log.info("<Sended> " + mess);
		
		String message = encoder.encode(mess, this); 
		
		byte[] bytes = message.getBytes();
		DatagramPacket out = new DatagramPacket(bytes,bytes.length,ip,port);
		
		udpSocket.send(out);
	}

	@Override
	public byte[] receive() throws IOException {
		byte[] buffer = new byte[Client.BUFFER];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		if (firstPacket == null)
			udpSocket.receive(packet);
		else
			packet = firstPacket;
		
		byte[] ret = new byte[buffer.length];
		
		System.arraycopy(buffer, 0, ret, 0, buffer.length);
		
		firstPacket = null;
		
		return ret;
	}
	
	@Override
	public void rawdataSend(byte[] bytes) throws IOException {
		DatagramPacket p = new DatagramPacket(bytes, bytes.length, ip, port);
		
		udpSocket.send(p);
	}
	
	@Override
	public String getIP() {
		return ip.getHostAddress();
	}
	
	@Override
	public int getPort() {
		return port;
	}
	
	@Override
	public void closeClient() {
		super.closeClient();

		udpSocket.close();
	}
}