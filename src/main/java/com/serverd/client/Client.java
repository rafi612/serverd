package com.serverd.client;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.serverd.log.Log;
import com.serverd.plugin.Encoder;
import com.serverd.plugin.Plugin;
import com.serverd.plugin.PluginManager;
import com.serverd.plugin.command.Command;
import com.serverd.plugin.listener.ExecutionController;

/**
 * Client class
 */
public class Client implements Runnable
{
	Thread thread;
	
	private int id;
	
	/** Connected */
	protected boolean connected;
	private boolean crashed = false;
	
	private int joinedid = -1;
	
	/** Max buffer size */
	public static final int BUFFER = 65536;
	
	private String name;
	
	/** Logger */
	public Log log,programlog;
	
	private boolean onceJoin = false;
	
	/** Client's encoder*/
	protected Encoder encoder;
	
	/**
	 * Client type
	 */
	public enum Type
	{
		SENDER,RECEIVER,NONE;
	}
	/**
	 * Client protocol
	 */
	public enum Protocol
	{
		TCP("TCP"),UDP("UDP"),CUSTOM("");
		
		public String name;
		
		Protocol(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
		
		public void setName(String name)
		{
			this.name = name;
		}
	}
	
	/** Client's type*/
	protected Type type = Type.NONE;
	/** Client's protocol*/
	protected Protocol protocol;
	
	/**
	 * Client class constructor
	 * @param id Client ID
	 */
	public Client(int id)
	{
		this.id = id;
		
		connected = true;
		name = "Client " + id;
		
		log = new Log("Client Thread " + id);
		programlog = new Log("Client Program " + id);
		
		encoder = new Encoder();
	}
	
	/**
	 * Setting encoder on client
	 * @param encoder Encoder instance
	 */
	public void setEncoder(Encoder encoder)
	{
		this.encoder = encoder;
	}
	
	/**
	 * Returns client encoder
	 * @return Client's Encoder
	 */
	public Encoder getEncoder()
	{
		return encoder;
	}
	
	/**
	 * Spliting text into words array
	 * @param text
	 * @return Array of words
	 */
	public String[] getWords(String text)
	{
		return text.split(" ");
	}

	/**
	 * Checking amount of arguments
	 * @param args Arguments
	 * @param length Arguments length
	 * @return Good of arguments
	 */
	protected boolean checkArgs(String[] args,int length) throws IOException
	{
		if (args.length < length) 
		{
			send("Missing Argument");
			return false;
		}
		else if (args.length > length)
		{
			send("Too much Arguments");
			return false;
		}
		else return true;
	}
	/**
	 * Receiving message
	 * @return Received message
	 * @throws IOException when socket throw error
	 */
	public String receive() throws IOException
	{
		return "";
	}
	
	/**
	 * Sending message
	 * @param mess Message to send
	 * @throws IOException when socket throw error
	 */
	public void send(String mess) throws IOException
	{

	}
	/**
	 * Receiving raw data
	 * @param buflen Buffer length
	 * @return byte array of data
	 * @throws IOException when socket throw error
	 */
	public byte[] rawdata_receive(int buflen) throws IOException
	{
		return new byte[buflen];
	}
	
	/**
	 * Sending raw data
	 * @param bytes Byte array
	 * @throws IOException when socket throw error
	 */
	public void rawdata_send(byte[] bytes) throws IOException
	{

	}
	
	/**
	 * Closing socket
	 */
	public void closeClient()
	{
		connected = false;
	}
	
	/**
	 * Returns client ID
	 * @return Client's IP
	 */
	public String getIP()
	{
		return "";
	}
	
	/**
	 * Returns client connected port
	 * @return Client's port
	 */
	public int getPort()
	{
		return 0;
	}
	
	/**
	 * Returns client connection state
	 * @return true if client is connected
	 */
	public boolean isConnected()
	{
		return connected;
	}
	
	/**
	 * Returns client joined state
	 * @return true if client is joined
	 */
	public boolean isJoined()
	{
		return joinedid != -1;
	}
	
	/**
	 * State of once join
	 * @return true if client is once joined
	 * @see Client#onceJoin
	 */
	public boolean isOnceJoined()
	{
		return onceJoin;
	}
	
	/**
	 * Returns the ID of the client that is joined
	 * @return Client's joined ID
	 */
	public int getJoinedID()
	{
		return joinedid;
	}
	
	/**
	 * Returns client ID
	 * @return Client's ID 
	 */
	public int getID()
	{
		return id;
	}
	
	/**
	 * Returns client name
	 * @return Client's name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Setting client name
	 * @param name Client new name
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Returns client protocol enum
	 * @return Client protocol enum
	 */
	public Protocol getProtocol()
	{
		return protocol;
	}
	
	/**
	 * Returns client type enum
	 * @return Client type enum
	 */
	public Type getType()
	{
		return type;
	}
	
	/**
	 * Join exception
	 */
	public class JoinException extends Exception 
	{
		private static final long serialVersionUID = 1L;

		/**
		 * JoinException class constructor
		 * @param message Message
		 */
		public JoinException(String message)
		{
			super(message);
		}
	}
	
	/**
	 * Joining to another client
	 * @param joinid Client ID to join
	 * @throws JoinException when join error occur 
	 */
	public void join(int joinid) throws JoinException
	{		
		Client cl = ClientManager.getClient(joinid);
		
		if (cl == null)
			throw new JoinException("Can't join: wrong client ID");
		
		if (isJoined())
			throw new JoinException("Can't join: client already joined");
		
		joinedid = joinid;
		type = Type.SENDER;
		
		cl.joinedid = id;
		cl.type = Type.RECEIVER;
	}
	
	/**
	 * Unjoining client
	 */
	public void unjoin()
	{
		Client cl = ClientManager.getClient(joinedid);
		
		if (cl == null)
			return;
		
		cl.joinedid = -1;
		cl.type = Type.NONE;
		
		joinedid = -1;
		type = Type.NONE;
	}
	
	/**
	 * Join once to client, after receive response, 
	 * client will disconnect automatically (used by <b>/to</b> command)
	 * @param joinid Client ID to join once
	 * @throws JoinException when join error occur 
	 */
	public void onceJoin(int joinid) throws JoinException
	{
		if (joinid == id)
			throw new JoinException("Can't join to self");
		
		onceJoin = true;
		
		join(joinid);
	}
	
	
	/**
	 * Returning status 
	 * @return Status message
	 */
	public String status()
	{
		return name + ": ID:" + id + " Connected:" + connected + " Joined:" + joinedid + " Type:" + type.toString() + " Protocol:" + protocol.getName() +" IP:" + getIP() + ":" + getPort() +"\n";
	}
	
	/**
	 * Crash handler
	 * @param exception Exception
	 */
	protected void crash(Exception exception)
	{
		if (!crashed && connected)
		{
			if (isJoined())
				unjoin();
			
			crashed = true;
			log.error("Client " + id + " crashed: " + exception.getMessage());
			
			closeClient();
			ClientManager.delete(id);
		}
	}
	
	@Override
	public void run() 
	{
		try 
		{
			log.info("Started working.");
			
			while (connected)	
				executeCommand(receive());
		} 
		catch (Exception e) 
		{
			crash(e);
		}
	}
	
	
	/**
	 * Executes command on client
	 * @param command_str Command to execute
	 * @throws Exception
	 */
	public void executeCommand(String command_str) throws Exception
	{
		String[] command_raw = getWords(command_str);
		String[] args = Arrays.copyOfRange(command_raw,1,command_raw.length);
		String command = command_raw[0];
		
		//execution controller
		boolean command_accepted = true;
		for (Plugin p : PluginManager.plugins)
		{
			if (!command_accepted)
				break;
			
			for (ExecutionController e : p.executioncontrollers)
			{
				if (!e.controlCommand(command, args, this, p))
				{
					command_accepted = false;
					break;
				}
			}
		}

		if (!command_accepted)
			return;
		
		switch (command)
		{
			case "/disconnect": 
			{
				//unjoining
				if (isJoined())
					unjoin();
				
				closeClient();
				ClientManager.delete(id);
				
				break;
			}
			case "/id": { send(String.valueOf(id)); break; }
			case "/status": { send(ClientManager.statusall()); break;}
			case "/to":
			{
				if (args.length < 1) 
					send("Missing Argument");
				else
				{
					String com = String.join(" ", Arrays.copyOfRange(args,1,args.length));
					int id = Integer.parseInt(args[0]);

					Client client = ClientManager.getClient(id);
					try 
					{
						onceJoin(id);
						client.send(com);
					}
					catch (JoinException e)
					{
						send(e.getMessage());
					}
				}
				break;
			}
			case "/join": 
			{
				if (checkArgs(args, 1))
				{
					try
					{
						join(Integer.parseInt(args[0]));
						send("Joined, now you are in joined mode.");
					}
					catch (JoinException e)
					{
						send(e.getMessage());
					}
						
				}
				break;
			}
			case "/close":
			{
				if (checkArgs(args, 1))
				{
					int closeid = Integer.parseInt(args[0]);
					
					ClientManager.delete(closeid);
					
					send("Client " + closeid + " closed.");
				}
				break;
			}
			case "/unjoin": 
			{	
				unjoin();
				send("Returning back to command mode");
				break;
			}
			case "/rawdata":
			{
				if (checkArgs(args, 1))
				{
					send("Done");
					
					int buffersize = Integer.parseInt(args[0]);

                    Client joined = ClientManager.clients.get(joinedid);
					
					if (joinedid != -1)
					{
						log.info("Raw data mode started," + buffersize+ " bytes can be sended");
						int i = 0;
						while (i < buffersize)
						{
							byte[] buffer = rawdata_receive(BUFFER);
							joined.rawdata_send(buffer);
	
							i+= buffer.length;
							//Log.log("ClientThread " + id,"Transfered byte " + i + "/" + buffersize + " Value:" + buffer);
						}
						send("Raw data mode closed");
					}
					else send("Error: you must be joined!");
				}
				break;
			}
			case "/setname":
			{
				if (args.length < 1) 
					send("Missing Argument");
				else
				{					
					name = String.join(" ", args);
					
					send("Name has been set to \"" + name + "\"");
				}
				break;
			}
			case "/plugin":
			{
				if (checkArgs(args, 2))
				{
					Plugin p = PluginManager.getByFileName(args[1]);
					
					if (p == null)
					{
						send("Plugin " + args[1] + " not found");
					}
					else if (args[0].equals("enable"))
					{
						
						if (p.isRunned())
						{
							send("Plugin " + args[1] + " is runned now");
						}
						else
						{
							if (PluginManager.enablePlugin(p))
								send("Plugin load succesfully");
							else
								send("Plugin load failed");
						}
					}
					else if (args[0].equals("disable"))
					{
						if (!p.isRunned())
						{
							send("Plugin " + args[1] + " is stopped now");
						}
						else 
						{
							PluginManager.disablePlugin(p);
							send("Plugin was disabled");
						}
					}
					else if (args[0].equals("info"))
					{
						String message = "=============\n" + args[1] + ":\n=============\n" + p.getInfo().toString();
						send(message);
					}
				}
				break;
			}
			case "/plugins-list":
			{
				String message = "";
				String[] plu = PluginManager.listPluginsName();
				
				String absolutepath = new File(PluginManager.pluginDir).getCanonicalPath();
				
				message += "Plugins installed in: " + absolutepath + "\n";
				
				if (plu.length > 0) 
				{
					for (String s : plu) 
					{
						message += s + "\tEnable:" + PluginManager.getByFileName(s).isRunned() + "\n";
					}
					send(message);
				}
				else send("No plugins installed in " + absolutepath);
				break;
			}
			case "/help":
			{
				String help = "/disconnect - disconnect client\n"
						+ "/id - shows id\n"
						+ "/status - shows status of all clients\n"
						+ "/to <id> <command> - sending command without joining to client\n"
						+ "/join <id> - join to client\n"
						+ "/close <id> - close another client connection\n"
						+ "/unjoin - unjoin current client\n"
						+ "/rawdata <buffer> - run rawdata mode with buffer\n"
						+ "/plugin <enable|disable|info> <filename> - manage installed plugins\n"
						+ "/plugins-list - list of loaded plugins\n"
						+ "/setname <name> - setting name\n"
						+ "/help - showing help\n";
				
				for (Plugin p : PluginManager.plugins)
					for (Command com : p.commands)
						help += com.help + "\n";
				
				send(help);
				break;
			}
			default:
			{
				boolean command_execute = false;
				//execute commands from plugins
				for (Plugin p : PluginManager.plugins)
					for (Command com : p.commands)
				{
					if (command.equals(com.command))
					{
						com.execute(args, this, p);
						command_execute = true;
					}
				}
				
				//if command isn't plugin command
				if (!command_execute) 
				{
					if (joinedid == -1)
						send("Client " + id + " not joined. Unknown command.");
					else 
					{
						ClientManager.clients.get(joinedid).send(command_str);
						
						if (onceJoin)
							unjoin();
					} 
				}
			}
		}
	}
}
