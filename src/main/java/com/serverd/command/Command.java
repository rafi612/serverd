package com.serverd.command;

import java.io.IOException;

import com.serverd.client.Client;
import com.serverd.client.Client.SendContinuation;
import com.serverd.plugin.Plugin;

/**
 * Abstract class to creating custom commands.
 */
public abstract class Command implements Codes,Cloneable {
	/** Command name*/
	public String command = "";
	/** Command help*/
	public String help = "";
	/** Default constructor*/
	public Command() {}
	
	/** Less arguments */
	protected static final int ARGS_LESS = 0b00000001;
	/** Good arguments */
	protected static final int ARGS_GOOD = 0b00000010; 
	/** More arguments */
	protected static final int ARGS_MORE = 0b00000100; 
	
	private boolean runned = false;
	
	/**
	 * Receive continuation interface.
	 */
	@FunctionalInterface
	public interface ReceiveContinuation {
		void receiveDone(byte[] bytes) throws IOException;
	}
	
	private ReceiveContinuation receiveHandler;
	
	/**
	 * Checking amount of arguments.
	 * @param args Arguments
	 * @param length Arguments length
	 * @param flag One of {@link Command#ARGS_LESS},{@link Command#ARGS_GOOD},{@link Command#ARGS_MORE}
	 * @return true when arguments are valid
	 */
	protected boolean checkArgs(String[] args,int length,int flag) {
		int argsCount = args.length;

		if (argsCount < length)
			return (flag & ARGS_LESS) == ARGS_LESS;
		else if (argsCount > length)
			return (flag & ARGS_MORE) == ARGS_MORE;
		else
			return (flag & ARGS_GOOD) == ARGS_GOOD;
	}
	
	/**
	 * Checking amount of arguments, using {@link Command#ARGS_GOOD} as default.
	 * @param args Arguments
	 * @param length Arguments length
	 * @return true when arguments are valid
	 */
	protected boolean checkArgs(String[] args,int length) {
		return checkArgs(args,length,ARGS_GOOD);
	}
	
	/**
	 * Checking amount of arguments and sending message to client.
	 * @param args Arguments
	 * @param client Client instance 
	 * @param length Arguments length
	 * @param flag One of {@link Command#ARGS_LESS},{@link Command#ARGS_GOOD},{@link Command#ARGS_MORE}
	 * @return true when arguments are valid
	 * @throws IOException when client throw {@link IOException}.
	 */
	protected boolean checkArgs(String[] args,Client client,int length,int flag) throws IOException {
		int argsCount = args.length;

		if (argsCount < length) {
			if ((flag & ARGS_LESS) == ARGS_LESS)
				return true;
			else 
				send(client,error("Too few arguments"));
			return false;
		} else if (argsCount > length) {
			if ((flag & ARGS_MORE) == ARGS_MORE) 
				return true;
			else 
				send(client,error("Too many arguments"));
			return false;
		} else {
			if ((flag & ARGS_GOOD) == ARGS_GOOD)
				return true;
			else 
				send(client,error("Bad number of arguments"));
			return false;
		}
	}
	
	/**
	 * Checking amount of arguments and sending message to client, using {@link Command#ARGS_GOOD} as default.
	 * @param args Arguments
	 * @param client Client instance 
	 * @param length Arguments length
	 * @return true when arguments are valid
	 * @throws IOException when client throw {@link IOException}.
	 */
	protected boolean checkArgs(String[] args,Client client,int length) throws IOException {
		return checkArgs(args,client,length,ARGS_GOOD);
	}
	
	/**
	 * Processing receive. Executing by server to process messages.
	 * @param bytes Bytes to process.
	 * @throws IOException when I/O error occurs.
	 */
	public void processReceive(byte[] bytes) throws IOException {
		receiveHandler.receiveDone(bytes);
	}
	
	/**
	 * @return command name.
	 */
	public String getName() {
		return command;
	}
	
	/**
	 * Setting command state as done.
	 */
	public void done() {
		runned = false;
	}
	
	/**
	 * @return true if command is runned.
	 */
	public boolean isRunned() {
		return runned;
	}
	
	/**
	 * Setting if command is runned.
	 * @param runned Runned
	 */
	public void setRunned(boolean runned) {
		this.runned = runned;
	}
	
	/**
	 * Executing when command is called.
	 * @param args Command arguments
	 * @param client Current client instance
	 * @param plugin Plugin instance
	 * @throws IOException when client throw {@link IOException}.
	 */
	public abstract void execute(String[] args,Client client,Plugin plugin) throws IOException;
	
	/**
	 * Wrapping {@link Client#send(String) method} in commands,
	 * automatically done command after sending.
	 * @param client Client instance
	 * @param message Message to send
	 * @throws IOException when I/O error occurs.
	 */
	public void send(Client client,String message) throws IOException {
		send(client,message, this::done);
	}
	
	/**
	 * Wrapping {@link Client#send(String, SendContinuation)} method in commands.
	 * @param client Client instance
	 * @param message Message to send
	 * @param continuation Send continuation executed after send complete
	 * @throws IOException when I/O error occurs.
	 */
	public void send(Client client,String message,SendContinuation continuation) throws IOException {
		client.send(message, continuation);
	}
	
	/**
	 * Wrapping {@link Client#rawdataSend(byte[], SendContinuation)} method in commands,
	 * automatically done command after sending.
	 * @param client Client instance
	 * @param bytes Bytes to send
	 * @throws IOException when I/O error occurs.
	 */
	public void send(Client client,byte[] bytes) throws IOException {
		send(client,bytes, this::done);
	}
	
	/**
	 * Wrapping {@link Client#rawdataSend(byte[], SendContinuation)} method in commands.
	 * @param client Client instance
	 * @param bytes Bytes to send
	 * @param continuation Send continuation executed after send complete
	 * @throws IOException when I/O error occurs.
	 */
	public void send(Client client,byte[] bytes,SendContinuation continuation) throws IOException {
		client.rawdataSend(bytes, continuation);
	}
	
	/**
	 * Receiving message when client ready.
	 * @param continuation Executing when receive message is ready.
	 */
	public void receive(ReceiveContinuation continuation) {
		receiveHandler = continuation;
	}
	
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}