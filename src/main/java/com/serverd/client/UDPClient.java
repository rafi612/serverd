package com.serverd.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Client class implementing UDP protocol.
 */
public class UDPClient extends SelectableClient {
	/** UDP Socket*/
	protected DatagramChannel udpSocket;
	
	/** Packet address*/
	protected InetSocketAddress address;

	/**
	 * UDPClient class constructor.
	 * @param id Client's ID
	 * @param selector Selector instance
	 * @param udpSocket Datagram socket instance
	 * @param address Client's address
	 */
	public UDPClient(int id, ClientManager clientManager,Selector selector, DatagramChannel udpSocket, SocketAddress address) {
		super(id,clientManager,selector);
		
		this.udpSocket = udpSocket;
		this.address = (InetSocketAddress) address;
		
		protocol = Protocol.UDP;
	}
	
	@Override
	public void send(String mess,SendContinuation continuation) throws IOException {
		processor.printSendMessage(mess);

		send(mess.getBytes(),continuation);
	}
	
	@Override
	public byte[] receive() throws IOException {
		receiveBuffer.clear();
		udpSocket.receive(receiveBuffer);
		receiveBuffer.flip();
			
		byte[] ret = new byte[receiveBuffer.limit()];
		receiveBuffer.get(ret, 0, receiveBuffer.limit());
		
		return ret;
	}
	
	@Override
	public void send(byte[] bytes, SendContinuation continuation) {
		getKey().interestOps(SelectionKey.OP_WRITE);
		
		queueBuffer(bytes,continuation);

		selector.wakeup();
	}
	
	@Override
	public SelectionKey getKey() {
		return udpSocket.keyFor(selector);
	}
	
	@Override
	public String getIP() {
		return address.getAddress().getHostAddress();
	}
	
	@Override
	public int getPort() {
		return address.getPort();
	}
	
	@Override
	public void closeClient() {
		super.closeClient();
		try {
			udpSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void processSend(ByteBuffer buffer) throws IOException {
		udpSocket.send(buffer,address);
	}
}