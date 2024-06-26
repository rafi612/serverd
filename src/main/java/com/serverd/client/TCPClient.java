package com.serverd.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * Client class implementing TCP protocol.
 */
public class TCPClient extends AsyncClient {	
	
	/** Socket*/
	protected AsynchronousSocketChannel tcpSocket;
	/** Timeout*/
	protected int timeout;
	
	/**
	 * TCPClient class constructor.
	 * @param id Client's ID.
	 * @param socket Socket instance.
	 */
	public TCPClient(int id,ClientManager clientManager,AsynchronousSocketChannel socket,int timeout) {
		super(id,clientManager);
		this.timeout = timeout;
		
		protocol = Protocol.TCP;
		tcpSocket = socket;
	}
	
	@Override
	public void send(String mess,SendContinuation continuation) throws IOException {
		processor.printSendMessage(mess);
		
		send(mess.getBytes(),continuation);
	}
	
	@Override
	public void receive(ReceiveComplete handler) {
		receiveBuffer.clear();
		
		readPending = true;
		tcpSocket.read(receiveBuffer,timeout,TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer, Void>() {

			@Override
			public void completed(Integer len, Void attachment) {
				receiveBuffer.flip();
				if (len == -1) {
					crash(new IOException("Connection closed"));
					return;
				}
				
				byte[] ret = new byte[len];
				receiveBuffer.get(ret, 0, len);
				
				readPending = false;
				handler.receiveDone(ret);	
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				readPending = false;
				// Check if exception is timeout
				if (exc instanceof InterruptedByTimeoutException)
					crash(new IOException("Read timed out"));
				else
					crash((Exception)exc);
			}
		});
	}
	
	@Override
	public void send(byte[] bytes, SendContinuation continuation) {
		writeBuffer.clear();
		writeBuffer.put(bytes);
		writeBuffer.flip();
		
		tcpSocket.write(writeBuffer, null, new CompletionHandler<Integer, Void>() {
			@Override
			public void completed(Integer bytesWritten, Void attachment) {
				if (writeBuffer.hasRemaining()) {
					tcpSocket.write(writeBuffer, null, this);
				} else {
					writeBuffer.clear();
					try {
						continuation.invoke();
					} catch (IOException e) {
						crash(e);
						return;
					}
					
					if (autoRead)
						unlockRead();
				}
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				crash((Exception)exc);
			}
			
		});
	}
	
	@Override
	public void closeClient() {
		super.closeClient();
		
		try {
			tcpSocket.close();
		} catch (IOException e) {
			log.error("Client closing failed: " + e.getMessage());
		}
	}
	
	
	@Override
	public String getIp() {
		try {
			return ((InetSocketAddress) tcpSocket.getRemoteAddress()).getAddress().getHostAddress();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int getPort() {
		try {
			return ((InetSocketAddress) tcpSocket.getRemoteAddress()).getPort();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
