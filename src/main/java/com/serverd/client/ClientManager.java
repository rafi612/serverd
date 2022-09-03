package com.serverd.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.serverd.client.Client.Protocol;
import com.serverd.log.Log;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.listener.ConnectListener;
import com.serverd.plugin.listener.UpdateIDListener;

public class ClientManager
{
	public static ArrayList<Client> clients = new ArrayList<>();
	
	public static int clients_connected = 0;
	public static int tcp_connected = 0;
	public static int udp_connected = 0;
	
	public static boolean tcpRunned = false,udpRunned = false;
	
	private static Log log = new Log("ServerD");
	
	private static boolean tcpenable = true,udpenable = true;
	
	public static void start(String ip,int tcpport,int udpport)
	{		
		Thread tcp = new Thread(() -> startTcpServer(ip, tcpport),"TCP Server");
		Thread udp = new Thread(() -> startUdpServer(ip, udpport),"UDP Server");
		tcp.start();
		udp.start();
	}
	
	private static ServerSocket tcpSocket;
	
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
			if (!tcpenable)
			{
				tcplog.info("TCP server was disabled");
				return;
			}
			
			tcpSocket = new ServerSocket(port,50,InetAddress.getByName(ip));			
			while (tcpRunned)
			{
				
				Socket sock = tcpSocket.accept();
				tcplog.info("Connection accepted from client!");
				
				TCPClient client = new TCPClient(clients.size(),sock);
				clients.add(client);
				
				clients_connected++;
				tcp_connected++;
				
				//plugin connect listener
				for (Plugin p : PluginManager.plugins)
					for (ConnectListener cl : p.connectlisteners)
						cl.onConnect(p,client);
				
				tcplog.info("Creating client thread...");
				client.thread.start();
			}
			tcpSocket.close();
		} 
		catch (IOException e)
		{
			if(tcpRunned)
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
	
	private static DatagramSocket udpSocket;
	
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
			if (!udpenable)
			{
				udplog.info("UDP server was disabled");
				return;
			}
			
			udpSocket = new DatagramSocket(null);
			udpSocket.setReuseAddress(true);
			udpSocket.bind(new InetSocketAddress(ip,port));
			
			while (udpRunned)
			{
				byte[] buffer = new byte[Client.BUFFER];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				udpSocket.receive(packet);
				
				String msg = new String(packet.getData(),packet.getOffset(),packet.getLength());
								
				udplog.info("Connection founded in " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " Message: " + msg);	
					
				UDPClient client = new UDPClient(clients.size(), udpSocket, packet, packet.getAddress(), packet.getPort());
				clients.add(client);
					
				clients_connected++;
				udp_connected++;
					
				//plugin connect listener
				for (Plugin p : PluginManager.plugins)
					for (ConnectListener cl : p.connectlisteners)
						cl.onConnect(p,client);
					
				udplog.info("Creating client thread...");
				client.thread.start();
				
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
	 */
	public static void stopUdpServer()
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
		if (clients.size() == 0 || clientid > clients.size())
			return;
		
		//stopping
		Client c = getClient(clientid);

		//plugin connect listener
		for (Plugin p : PluginManager.plugins)
			for (ConnectListener cl : p.connectlisteners)
			{
				try 
				{
					cl.onDisconnect(p,c);
				} 
				catch (IOException e) 
				{
					log.error("Error in Disconnect Listener: " + e.getMessage());
				}
			}
			
		c.closeClient();
			
		clients_connected--;
		if (c.protocol == Protocol.TCP)
			tcp_connected--;
		else
			udp_connected--;

		clients.remove(clientid);
			
		
		//updating id
		for (int i = 0;i < clients.size();i++)
		{
			Client cl = getClient(i);
				
			for (Plugin p : PluginManager.plugins)
				for (UpdateIDListener u : p.updateidlisteners)
					u.updateID(p,cl.id, i);
				
			cl.id = i;
			cl.joinedid = clients.lastIndexOf(cl.joiner);
		}
			
		log.info("Client " + clientid + " has been closed");

	}
	
	public static void shutdown()
	{
		try
		{	
			log.info("Server shutting down...");
			stopTcpServer();
			stopUdpServer();
			
			log.info("Closing clients...");
			for (Client client : clients)
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
	
	public static Client getClient(int id)
	{
		if (id < 0 || id > clients.size() - 1)
			return null;
		return clients.get(id);
	}
	
	public static String statusall()
	{
		String message = clients.size() == 0 ? "No clients connected" : "";
		
		for (Client client : clients) 
		{
			message += client.status();
		}
		return message;
	}
	
	public static void setTCPEnable(boolean enable)
	{
		tcpenable = enable;
	}
	
	public static  void setUDPEnable(boolean enable)
	{
		udpenable = enable;
	}
	
	public static boolean isTCPEnable()
	{
		return tcpenable;
	}
	
	public static boolean isUDPEnable()
	{
		return tcpenable;
	}

}
