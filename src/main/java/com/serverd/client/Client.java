package com.serverd.client;

import java.io.File;
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
	public Thread thread;
	
	public int id;
	
	public boolean connected;
	private boolean crashed = false;
	public int joinedid = -1;
	
	Client joiner = null;
	
	public static final int BUFFER = 65536;
	
	public String name;
	
	public Log log;
	
	boolean onceJoin = false;
	
	Encoder encoder;
	
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
	
	public Type type = Type.NONE;
	public Protocol protocol;
	
	/**
	 * Client class constructor
	 * @param id Client ID
	 */
	public Client(int id)
	{
		connected = true;
		
		this.id = id;
		
		name = "Client " + id;
		
		log = new Log("ClientThread " + id);
		
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
	protected boolean checkArgs(String[] args,int length)
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
	 */
	public String receive()
	{
		return "";
	}
	
	/**
	 * Sending message
	 * @param mess Message to send
	 */
	public void send(String mess)
	{

	}
	/**
	 * Receiving raw data
	 * @param buflen Buffer length
	 * @return byte array of data
	 */
	public byte[] rawdata_receive(int buflen)
	{
		return new byte[buflen];
	}
	
	/**
	 * Sending raw data
	 * @param b Byte array
	 */
	public void rawdata_send(byte[] b)
	{

	}
	
	/**
	 * Closing client
	 */
	public void closeSocket()
	{
		
	}
	
	/**
	 * Closing socket
	 */
	public void closeClient()
	{

	}
	
	/**
	 * Return Client's IP
	 * @return Client's IP
	 */
	public String getIP()
	{
		return "";
	}
	
	/**
	 * Return Client's port
	 * @return Client's port
	 */
	public int getPort()
	{
		return 0;
	}
	
	
	/**
	 * Joining to another client
	 * @param targetid Client ID to join
	 * @return Exit code (0 if succesfully joined)
	 */
	public int join(int joinid)
	{
		Client cl = ClientManager.getClient(joinid);
		
		if (cl == null)
			return 1;
		
		joinedid = joinid;
		type = Type.SENDER;
		joiner = cl;
		
		cl.joinedid = id;
		cl.type = Type.RECEIVER;
		cl.joiner = this;
		
		return 0;
	}
	
	/**
	 * Unjoining client
	 */
	public void unjoin()
	{
		Client cl = ClientManager.getClient(joinedid);
		cl.joinedid = -1;
		cl.type = Type.NONE;
		cl.joiner = null;
		
		joinedid = -1;
		type = Type.NONE;
		joiner = null;
	}
	
	/**
	 * Setting if client is Joined once. After receiving of message client will be automatic unjoin.
	 * @param oncejoin true/false value
	 * @param joinid Once join client ID
	 */
	public void setOnceJoin(boolean oncejoin,int joinid)
	{
		if (joinedid != -1)
			return;
		
		onceJoin = oncejoin;
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
	 * @param e Exception
	 */
	protected void crash(Exception e)
	{
		if (!crashed && connected)
		{
			connected = false;
			crashed = true;
			log.log("Client " + id + " crashed: " + e.getMessage());
			
			closeClient();
			ClientManager.delete(id);
		}
	}
	
	@Override
	public void run() 
	{
		try 
		{
			clientLoop();
		} 
		catch (Exception e) 
		{
			crash(e);
		}
	}
	
	/**
	 * Client's main loop
	 * @throws Exception
	 */
	public void clientLoop() throws Exception
	{
		log.log("Started working.");
		while (connected)
		{			
			String command_str = receive();
			
			if (command_str.equals(""))
				throw new Exception("Empty buffer");
		
			executeCommand(command_str);
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
				String message = e.controlCommand(command, this, p);
				
				if (!message.equals("") && message != null)
				{
					send(message);
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
				if (joinedid != -1)
				{
					ClientManager.clients.get(joinedid).joinedid = -1;
					ClientManager.clients.get(joinedid).type = Type.NONE;
					
					joinedid = -1;
					type = null;
				}
				closeClient();
				ClientManager.delete(id);
				break;
			}
			case "/id": { send("your id is: " + id); break; }
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
					client.setOnceJoin(true, this.id);
					client.send(com);
				}
				break;
			}
			case "/join": 
			{
				if (checkArgs(args, 1))
				{
					if (join(Integer.parseInt(args[0])) == 0)
						send("Joined, now you are in joined mode.");
					else 
						send("Can't join: wrong client ID");
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
						log.log("Raw data mode started," + buffersize+ " bytes can be sended");
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
					String newname = "";
					
					for (int i = 0;i < args.length;i++)
						newname = newname + args[i] + (i < args.length - 1 ? " " : "");
					
					name = newname;
					
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
						
						if (p.isRunned)
						{
							send("Plugin " + args[1] + " is runned now");
						}
						else
						{
							if (PluginManager.enablePlugin(p) != 0)
								send("Plugin load failed");
							else
								send("Plugin load succesfully");
						}
					}
					else if (args[0].equals("disable"))
					{
						if (!p.isRunned)
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
						String message = "=============\n" + args[1] + ":\n=============\n" + p.info.toString();
						send(message);
					}
				}
				break;
			}
			case "/plugins-list":
			{
				String message = "";
				String[] plu = PluginManager.listPluginsName();
				
				String absolutepath = new File(PluginManager.plugindir).getCanonicalPath();
				
				message += "Plugins installed in: " + absolutepath + "\n";
				
				if (plu.length > 0) 
				{
					for (String s : plu) 
					{
						message += s + "    Enable:" + PluginManager.getByFileName(s).isRunned + "\n";
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
						+ "/to <id> <command> - sending command without joining to client"
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
				{
					help += com.help + "\n";
				}
				
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