package com.serverd.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import com.serverd.client.Client.Protocol;
import com.serverd.config.Config;
import com.serverd.log.Log;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.listener.ConnectListener;

/**
 * Client Manager
 */
public class ClientManager {
	/** Client's hashmap*/
	public static HashMap<Integer,Client> clients = new HashMap<>();
	
	/** Client's connected amount*/
	public static int tcpConnected = 0,udpConnected = 0;
	
	static boolean tcpRunned = false,udpRunned = false;
	
	private static Log log = new Log("ServerD");
	
	private static boolean tcpEnabled = true,udpEnabled = true;
	
	/**
	 * Starting server
	 * @param ip IP
	 * @param tcpport TCP port
	 * @param udpport UDP port
	 */
	public static void start(String ip,int tcpport,int udpport,Config config) {		
		Thread tcp = new Thread(() -> startTcpServer(ip, tcpport,config),"TCP Server");
		Thread udp = new Thread(() -> startUdpServer(ip, udpport,config),"UDP Server");
		tcp.start();
		udp.start();
	}
	
	private static ServerSocket tcpSocket;
	
	/**
	 * Starting TCP Server
	 * @param ip IP of server
	 * @param port Port of server
	 */
	public static void startTcpServer(String ip,int port,Config config) {
		Log tcplog = new Log("ServerD TCP");
		
		if (!tcpEnabled || !config.enableTcp) {
			tcplog.info("TCP server was disabled");
			return;
		}
		
		tcplog.info("Starting TCP Server...");
		tcpRunned = true;

		try {
			tcpSocket = new ServerSocket(port,50,InetAddress.getByName(ip));			
			while (tcpRunned) {
				Socket sock = tcpSocket.accept();
				tcplog.info("Connection accepted from client!");
				
				TCPClient client = new TCPClient(getFreeClientID(),sock,config);
				addClient(client);
				
				tcpConnected++;
				setupClient(client);
				
				tcplog.info("Creating client thread...");
				new Thread(() -> {
					try {
						while (client.isConnected()) {
							byte[] bytes = client.receive();
							if (bytes != null)
								client.getProcessor().processCommand(bytes);
						}
					} catch (IOException e) {
						client.crash(e);
					}
				}).start();
			}
			tcpSocket.close();
		} catch (IOException e) {
			if (tcpRunned)
				tcplog.error("Server error: " + e.getMessage());
		}
	}
	
	/**
	 * Stopping TCP server
	 * @throws IOException 
	 */
	public static void stopTcpServer() throws IOException {
		tcpRunned = false;
		
		if (tcpSocket != null) {
			log.info("Stopping TCP server..");
			tcpSocket.close();
		}
	}
	
	private static DatagramSocket udpSocket;
	
	/**
	 * Starting UDP Server
	 * @param ip IP of server
	 * @param port Port of server
	 */
	public static void startUdpServer(String ip,int port,Config config) {
		Log udplog = new Log("ServerD UDP");
		
		if (!udpEnabled || !config.enableUdp) {
			udplog.info("UDP server was disabled");
			return;
		}
		
		udplog.info("Starting UDP Server...");
		
		udpRunned = true;
		
		try {	
			udpSocket = new DatagramSocket(null);
			udpSocket.setReuseAddress(true);
			udpSocket.bind(new InetSocketAddress(ip,port));
			
			while (udpRunned) {
				byte[] buffer = new byte[Client.BUFFER];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				udpSocket.receive(packet);
				
				String msg = new String(packet.getData(),packet.getOffset(),packet.getLength());
								
				udplog.info("Connection founded in " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " Message: " + msg);	
					
				UDPClient client = new UDPClient(getFreeClientID(), udpSocket, packet, packet.getAddress(), packet.getPort(),config);
				addClient(client);
					
				udpConnected++;
				
				setupClient(client);
					
				udplog.info("Creating client thread...");
				new Thread(() -> {
					try {
						while (client.isConnected()) {
							byte[] bytes = client.receive();
							if (bytes != null)
								client.getProcessor().processCommand(bytes);
						}
					} catch (IOException e) {
						client.crash(e);
					}
				}).start();
				
			}
			udpSocket.close();
		} catch (IOException e) {
			if (udpRunned)
				udplog.error("Server error: " + e.getMessage());
		}
	}
	
	/**
	 * Closing UDP server
	 */
	public static void stopUdpServer() {
		udpRunned = false;
		
		if (udpSocket != null) {
			log.info("Stopping UDP server..");
			udpSocket.close();
		}
	}
	
	/**
	 * Deleting client
	 * @param clientid Client ID to remove
	 */
	public static synchronized void delete(int clientid) {	
		if (clients.size() == 0)
			return;
		
		Client client = getClient(clientid);
		
		if (client.isJoined())
			client.unjoin();
		
		//plugin connect listener
		for (Plugin p : PluginManager.plugins)
			for (ConnectListener cl : p.connectListeners) {
				try {
					cl.onDisconnect(p,client);
				} catch (IOException e) {
					log.error("Error in Disconnect Listener: " + e.getMessage());
				}
			}
			
		client.closeClient();
		
		if (client.protocol == Protocol.TCP)
			tcpConnected--;
		else
			udpConnected--;	

		clients.remove(clientid);
			
		log.info("Client " + clientid + " has been closed");
	}
	
	/**
	 * Shutting down server
	 */
	public static void shutdown() {
		try {	
			log.info("Server shutting down...");
			stopTcpServer();
			stopUdpServer();
			
			log.info("Closing clients...");
			for (Client client : clients.values())
				client.closeClient();
			
			log.info("Stopping plugins...");
			for (Plugin plugin : PluginManager.plugins)
				plugin.stop();
			
		} catch (IOException e) {
			log.error("Error stopping server:" + e.getMessage());
		}
	}
	
	/**
	 * Searching first free client ID
	 * @return first free ID
	 */
	public static int getFreeClientID() {
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
	public static void setupClient(Client client) throws IOException {		
		//plugin connect listener
		for (Plugin p : PluginManager.plugins)
			for (ConnectListener cl : p.connectListeners)
				cl.onConnect(p,client);
	}
	
	/**
	 * Adding client
	 * @param client Client object
	 */
	public static void addClient(Client client) {
		clients.put(client.getID(),client);
	}
	
	/**
	 * Returning all clients.
	 * @return Array of clients
	 */
	public static Client[] getAllClients() {
		return clients.values().toArray(Client[]::new);
	}
	
	/**
	 * Returning clients amount
	 * @return clients amount number
	 */
	public static int getClientConnectedAmount() {
		return clients.size();
	}
	
	/**
	 * Returns client instance by ID
	 * @param id Client ID
	 * @return Client instance
	 */
	public static Client getClient(int id) {
		return clients.get(id);
	}
	
	/**
	 * Setting TCP server enabled
	 * @param enable true if TCP server may be enabled
	 * @see ClientManager#isTCPEnabled()
	 */
	public static void setTCPEnabled(boolean enable) {
		tcpEnabled = enable;
	}
	
	/**
	 * Setting UDP server enabled
	 * @param enable true if UDP server may be enabled
	 * @see ClientManager#isUDPEnabled()
	 */
	public static void setUDPEnabled(boolean enable) {
		udpEnabled = enable;
	}
	
	/**
	 * TCP server enabled state
	 * @return true id TCP Server is enabled
	 * @see ClientManager#setTCPEnabled
	 */
	public static boolean isTCPEnabled() {
		return tcpEnabled;
	}
	
	/**
	 * UDP server enabled state
	 * @return true id UDP Server is enabled
	 * @see ClientManager#setUDPEnabled
	 */
	public static boolean isUDPEnabled() {
		return tcpEnabled;
	}
}