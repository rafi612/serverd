package com.serverd.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.serverd.client.Client.Protocol;
import com.serverd.log.Log;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.listener.ConnectListener;
import com.serverd.plugin.listener.UpdateIDListener;
import com.serverd.util.Util;

public class ClientManager
{
	public static ArrayList<Client> clients = new ArrayList<>();
	public static ArrayList<UDPClient> udp_clients = new ArrayList<>();
	
	public static int clients_connected = 0;
	public static int tcp_connected = 0;
	public static int udp_connected = 0;
	
	public static boolean runned = false;
	
	private static Log log = new Log("ServerD");
	
	private static boolean tcpenable = true,udpenable = true;
	
	public static void start(String ip,int tcpport,int udpport)
	{		
		runned = true;
		
		Thread tcp = new Thread(() -> tcp_server(ip, tcpport),"TCP Server");
		Thread udp = new Thread(() -> udp_server(ip, udpport),"UDP Server");
		tcp.start();
		udp.start();
	}
	
	
	public static void tcp_server(String ip,int port)
	{
		Log tcplog = new Log("ServerD TCP");

		try 
		{
			if (!tcpenable)
			{
				tcplog.info("TCP server was disabled");
				return;
			}
			
			ServerSocket server = new ServerSocket(port,50,InetAddress.getByName(ip));			
			while (runned)
			{
				
				Socket sock = server.accept();
				tcplog.info("Connection accepted from client!");
				
				TCPClient client = new TCPClient(ClientManager.clients.size(),sock);
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
			server.close();
		} 
		catch (IOException e)
		{
			tcplog.error("Error while creating server: " + e.getMessage());
		}
	}
	
	public static void udp_server(String ip,int port)
	{
		Log udplog = new Log("ServerD UDP");
		
		try 
		{
			if (!udpenable)
			{
				udplog.info("UDP server was disabled");
				return;
			}
			
			DatagramSocket socket = new DatagramSocket(port);
			
			while (runned)
			{
				byte[] buffer = new byte[Client.BUFFER];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				socket.receive(packet);
				
				String msg = new String(packet.getData(),packet.getOffset(),packet.getLength());
				
				boolean new_ = true;
				
				for (UDPClient client : udp_clients)
				{
					if (client.ip.equals(packet.getAddress()) && client.port == packet.getPort())
					{						
						client.buffer = packet.getData();
						client.bufferOffset = packet.getOffset();
						client.bufferLength = packet.getLength();
						
						while (client.buffer != null)
							Util.sleep(1);
						
						new_ = false;
						break;
					}
				}
				
				if (new_)
				{					
					udplog.info("Connection founded in " + packet.getAddress().getHostAddress() + ":" + packet.getPort() +" Message: " + msg);
					
					UDPClient client = new UDPClient(ClientManager.clients.size(), socket, packet.getAddress(), packet.getPort());
					clients.add(client);
					udp_clients.add(client);
					
					//redirecting message to client loop
					client.buffer = packet.getData();
					client.bufferOffset = packet.getOffset();
					client.bufferLength = packet.getLength();
					
					clients_connected++;
					udp_connected++;
					
					//plugin connect listener
					for (Plugin p : PluginManager.plugins)
						for (ConnectListener cl : p.connectlisteners)
							cl.onConnect(p,client);
					
					udplog.info("Creating client thread...");
					client.thread.start();
				}
				
			}
			socket.close();
			
		} 
		catch (IOException e)
		{
			udplog.error("Error while creating server: " + e.getMessage());
		}
	}
	
	/**
	 * Deleting client
	 * @param clientid Client ID to remove
	 */
	public static void delete(int clientid)
	{	
		if (clients.size() == 0)
			return;
		
		//stopping
		Client c = getClient(clientid);

		//plugin connect listener
		for (Plugin p : PluginManager.plugins)
			for (ConnectListener cl : p.connectlisteners)
				cl.onDisconnect(p,c);
			
		c.closeClient();
			
		clients_connected--;
		if (c.protocol == Protocol.TCP)
			tcp_connected--;
		else
		{
			udp_connected--;
			udp_clients.remove(c);
		}

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
		log.info("Server shutting down...");
	
		log.info("Closing clients...");
		for (Client client : clients)
			client.closeClient();
		
		log.info("Stopping plugins...");
		for (Plugin plugin : PluginManager.plugins)
			plugin.stop();
	}
	
	public static Client getClient(int id)
	{
		if (id < 0 || id > clients.size())
			return null;
		return clients.get(id);
	}
	
	public static String statusall()
	{
		String message = clients.size() == 0 ? "No clients connected" : "";
		
		for (int i = 0;i < clients.size();i++) 
		{
			message += clients.get(i).status();
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
