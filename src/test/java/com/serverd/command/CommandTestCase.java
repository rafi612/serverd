package com.serverd.command;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.serverd.client.Client;
import com.serverd.client.ClientManager;
import com.serverd.plugin.PluginManager;

class CommandTestCase {
	TestClient testClient;
	
	@BeforeEach
	void setUp() throws Exception {
		CommandProcessor.commands.clear();
		PluginManager.plugins.clear();
		ClientManager.clients.clear();
		
		testClient = new TestClient();
		testClient.init();
	}
	
	@AfterEach
	void tearDown() throws Exception {
		testClient.destroy();
	}
	
	public static void executeTest(Command command,String[] args,TestClient client) throws Exception {
		String comm = command.command + " " + String.join(" ", args);
		client.log.info("Executing command: " + comm);
		
		CommandProcessor.commands.add(command);
		client.getProcessor().processCommand(comm.getBytes());
		
		while (((CommandProcessor)client.getProcessor()).getCurrentCommand() != null) 
			if (client.receiveIndex < client.receiveQueue.size())
				client.getProcessor().processCommand(client.rawdataReceive());
			else
				break;
		
		CommandProcessor.commands.remove(command);
	}
	
	public static void executeTest(Command command,TestClient client) throws Exception {
		executeTest(command,new String[] {},client);
	}
	
	public static String[] args(String... args) {
		return args;
	}
}

class DoubleClientCommandTestCase extends CommandTestCase {
	TestClient testClient2;
	
	@BeforeEach
	void setUp() throws Exception {
		super.setUp();
		testClient2 = new TestClient();
		testClient2.init();
	}
	@AfterEach
	void tearDown() throws Exception {
		super.tearDown();
		testClient2.destroy();
		ClientManager.clients.clear();
	}	
}

class TestClient extends Client {
	protected ArrayList<byte[]> receiveQueue = new ArrayList<>();
	private ArrayList<String> sendQueue = new ArrayList<>();
	
	private ArrayList<byte[]> rawDataSendQueue = new ArrayList<>();
	
	protected int receiveIndex;
	
	public TestClient() {
		super(ClientManager.getFreeClientID());
		protocol = Protocol.CUSTOM;
		connected = true;
	}
	
	public void init() {
		ClientManager.addClient(this);
	}
	
	public void destroy() {
		ClientManager.delete(getID());
	}
	
	@Override
	public byte[] rawdataReceive() {
		System.out.println(receiveIndex);
		return receiveQueue.get(receiveIndex++);
	}
	
	@Override
	public void send(String message,SendContinuation runnable) throws IOException {
		log.info("<Sended> " + message);
		sendQueue.add(message);
		runnable.invoke();
	}
	
	@Override
	public void rawdataSend(byte[] buffer,SendContinuation runnable) throws IOException {
		rawDataSendQueue.add(buffer);
		runnable.invoke();
	}
	
	public String[] getSend() {
		return sendQueue.toArray(String[]::new);
	}
	
	public ArrayList<byte[]> getRawdataSend() {
		return rawDataSendQueue;
	}
	
	public void insertRawdataReceive(byte[] bytes) {
		receiveQueue.add(bytes);
	}
	
	public void insertReceive(String message) {
		receiveQueue.add(message.getBytes());
	}
}