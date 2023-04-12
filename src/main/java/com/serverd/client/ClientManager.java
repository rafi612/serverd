package com.serverd.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

import com.serverd.client.Client.Protocol;
import com.serverd.log.Log;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.listener.ConnectListener;

/**
 * Client Manager
 */
public class ClientManager
{
	/** Client's hashmap*/
	public static HashMap<Integer,Client> clients = new HashMap<>();
	
	static boolean tcpRunned = false,udpRunned = false;
	
	private static Log log = new Log("ServerD");
	
	private static boolean tcpEnabled = true,udpEnabled = true;
	
	/**
	 * Starting server
	 * @param ip IP
	 * @param tcpport TCP port
	 * @param udpport UDP port
	 */
	public static void start(String ip,int tcpport,int udpport)
	{		
		Thread tcp = new Thread(() -> startTcpServer(ip, tcpport),"TCP Server");
		Thread udp = new Thread(() -> startUdpServer(ip, udpport),"UDP Server");
		tcp.start();
		udp.start();
	}
	
	public static ServerSocketChannel tcpSocket;
	
	/**
	 * Starting TCP Server
	 * @param ip IP of server
	 * @param port Port of server
	 */
	public static void startTcpServer(String ip,int port)
	{
		Log tcplog = new Log("ServerD TCP");
		
		tcplog.info("Starting TCP Server...");
		tcpRunned = true;

		try 
		{
			if (!tcpEnabled)
			{
				tcplog.info("TCP server was disabled");
				return;
			}
			
			tcpSocket = ServerSocketChannel.open();
			tcpSocket.bind(new InetSocketAddress(ip,port));
			
			tcpSocket.configureBlocking(false);
			
			Selector selector = Selector.open();
			tcpSocket.register(selector, SelectionKey.OP_ACCEPT);
			
			while (tcpRunned) {
				selector.select();
				
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				while (keys.hasNext()) {
					SelectionKey key = keys.next();
	                keys.remove();
	                
	                if (!key.isValid())
	                	continue;
	                
	                if (key.isAcceptable()) {
	                    SocketChannel socket = tcpSocket.accept();
	                    socket.configureBlocking(false);
	                    
	                    tcplog.info("Connection accepted from client!");
	                    
	    				TCPClient client = new TCPClient(getFreeClientID(),selector,socket);
	    				addClient(client);
	    				
	                    socket.register(selector, SelectionKey.OP_READ,client);
	    				
	    				setupClient(client);
	                }
	                
	                if (key.isReadable()) {
	                	TCPClient client = (TCPClient) key.attachment();
	                	try {
		                	client.processCommand(client.rawdataReceive());
	                	} catch (IOException e) {
	                		client.crash(e);
	                		continue;
	                	}
	                }
	               
	                if (!key.isValid())
	                	continue;
	                
	                if (key.isWritable()) {
	                	TCPClient client = (TCPClient) key.attachment();
	                	
	                	if (client.processQueue())
	                		key.interestOps(SelectionKey.OP_READ);
	                }
				}
			}
			
		} 
		catch (IOException e)
		{
			if (tcpRunned)
				tcplog.error("Server error: " + e.getMessage());
		}
	}
	
	/**
	 * Stopping TCP server
	 * @throws IOException 
	 */
	public static void stopTcpServer() throws IOException
	{
		tcpRunned = false;
		
		if (tcpSocket != null) 
		{
			log.info("Stopping TCP server..");
			tcpSocket.close();
		}
	}
	
	public static DatagramChannel udpSocket;
	
	/**
	 * Starting UDP Server
	 * @param ip IP of server
	 * @param port Port of server
	 */
	public static void startUdpServer(String ip,int port)
	{
		Log udplog = new Log("ServerD UDP");
		
		udplog.info("Starting UDP Server...");
		
		udpRunned = true;
		
		try 
		{
			if (!udpEnabled)
			{
				udplog.info("UDP server was disabled");
				return;
			}
			
			udpSocket = DatagramChannel.open();
			udpSocket.configureBlocking(false);
			udpSocket.bind(new InetSocketAddress(ip,port));
			
	        Selector selector = Selector.open();
	        udpSocket.register(selector, SelectionKey.OP_READ);
			
			while (udpRunned)
			{
	            selector.select();
	            
	            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
	            while (keys.hasNext()) {
	            	SelectionKey key = keys.next();
	            	keys.remove();
	            	
	                if (!key.isValid())
	                	continue;
	            	
	            	if (key.isReadable()) {
	            		DatagramChannel dc = (DatagramChannel) key.channel();
	            		ByteBuffer buffer = ByteBuffer.allocate(Client.BUFFER);
	            		InetSocketAddress address = (InetSocketAddress) dc.receive(buffer);
	            		buffer.flip();
	            		
	    				boolean new_ = true;
	    				
	    				for (Client client : clients.values())
	    					if (client.getProtocol() == Protocol.UDP)
	    						if (client.getIP().equals(address.getAddress().getHostAddress()) && client.getPort() == address.getPort()) {
	    							
	    		    				byte[] data = new byte[buffer.limit()];
	    		    				buffer.get(data, 0, buffer.limit());
	    		    				
	    							client.processCommand(data);
	    							
	    							new_ = false;
	    							break;
	    						}
	    				
	    				if (new_) {	
	    					UDPClient client = new UDPClient(getFreeClientID(), selector, udpSocket, address);
	    					addClient(client);
	    					
	    					udplog.info("Connection founded in " + client.getIP() + ":" + client.getPort());	
	    					
	    					setupClient(client);
	    					
		    				byte[] data = new byte[buffer.limit()];
		    				buffer.get(data, 0, buffer.limit());
	    					
	    					client.processCommand(data);
	    				}
	            	}
	            	
	                if (!key.isValid())
	                	continue;
	            	
	            	if (key.isWritable()) {
	            		for (Client client : clients.values())
	    					if (client.getProtocol() == Protocol.UDP) {
	    						UDPClient udpClient = (UDPClient) client;
	    	                	udpClient.processQueue();
	    					}
	            		key.interestOps(SelectionKey.OP_READ);
	            	}
	            }
			}
            udpSocket.close();

		} 
		catch (IOException e)
		{
			if (udpRunned)
				udplog.error("Server error: " + e.getMessage());
		}
	}
	
	/**
	 * Closing UDP server
	 * @throws IOException when channel throws {@link IOException}
	 */
	public static void stopUdpServer() throws IOException
	{
		udpRunned = false;
		
		if (udpSocket != null) 
		{
			log.info("Stopping UDP server..");
			udpSocket.close();
		}
	}
	
	/**
	 * Deleting client
	 * @param clientid Client ID to remove
	 */
	public static synchronized void delete(int clientid)
	{	
		if (clients.size() == 0)
			return;
		
		Client client = getClient(clientid);
		
		if (client.isJoined())
			client.unjoin();
		
		//plugin connect listener
		for (Plugin p : PluginManager.plugins)
			for (ConnectListener cl : p.connectlisteners)
			{
				try 
				{
					cl.onDisconnect(p,client);
				} 
				catch (IOException e) 
				{
					log.error("Error in Disconnect Listener: " + e.getMessage());
				}
			}
			
		client.closeClient();

		clients.remove(clientid);
			
		log.info("Client " + clientid + " has been closed");
	}
	
	/**
	 * Shutting down server
	 */
	public static void shutdown()
	{
		try
		{	
			log.info("Server shutting down...");
			stopTcpServer();
			stopUdpServer();
			
			log.info("Closing clients...");
			for (Client client : clients.values())
				client.closeClient();
			
			log.info("Stopping plugins...");
			for (Plugin plugin : PluginManager.plugins)
				plugin.stop();
		} 
		catch (IOException e) 
		{
			log.error("Error stopping server:" + e.getMessage());
		}
	}
	
	/**
	 * Searching first free client ID
	 * @return first free ID
	 */
	public static int getFreeClientID()
	{
		int i = 0;
		while (clients.containsKey(i))
			i++;
		return i;
	}
	
	/**
	 * Configures client and executing connect listener.
	 * Can be used in plugins on adding custom protocols.
	 * @param client {@link Client} instance
	 * @throws IOException when {@link ConnectListener} throws error
	 */
	public static void setupClient(Client client) throws IOException
	{		
		//plugin connect listener
		for (Plugin p : PluginManager.plugins)
			for (ConnectListener cl : p.connectlisteners)
				cl.onConnect(p,client);
	}
	
	/**
	 * Adding client
	 * @param client Client object
	 */
	public static void addClient(Client client)
	{
		clients.put(client.getID(),client);
	}
	
	/**
	 * Returning all clients.
	 * @return Array of clients
	 */
	public static Client[] getAllClients()
	{
		return clients.values().toArray(Client[]::new);
	}
	
	public static int getClientConnectedAmount()
	{
		return clients.size();
	}
	
	/**
	 * Returns client instance by ID
	 * @param id Client ID
	 * @return Client instance
	 */
	public static Client getClient(int id)
	{
		return clients.get(id);
	}
	
	/**
	 * Setting TCP server enabled
	 * @param enable true if TCP server may be enabled
	 * @see ClientManager#isTCPEnabled()
	 */
	public static void setTCPEnabled(boolean enable)
	{
		tcpEnabled = enable;
	}
	
	/**
	 * Setting UDP server enabled
	 * @param enable true if UDP server may be enabled
	 * @see ClientManager#isUDPEnabled()
	 */
	public static void setUDPEnabled(boolean enable)
	{
		udpEnabled = enable;
	}
	
	/**
	 * TCP server enabled state
	 * @return true id TCP Server is enabled
	 * @see ClientManager#setTCPEnabled
	 */
	public static boolean isTCPEnabled()
	{
		return tcpEnabled;
	}
	
	/**
	 * UDP server enabled state
	 * @return true id UDP Server is enabled
	 * @see ClientManager#setUDPEnabled
	 */
	public static boolean isUDPEnabled()
	{
		return tcpEnabled;
	}
}
