package com.serverd.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.concurrent.TimeUnit;

import com.serverd.config.Config;

/**
 * TCP client class.
 */
public class TCPClient extends AsyncClient {	
	
	/** Socket*/
	protected AsynchronousSocketChannel tcpSocket;
	/** Config*/
	protected Config config;
	
	/**
	 * TCPClient class constructor.
	 * @param id Client's ID
	 * @param socket Socket instance
	 */
	public TCPClient(int id,AsynchronousSocketChannel socket,Config config) {
		super(id);
		this.config = config;
		
		protocol = Protocol.TCP;
		tcpSocket = socket;
	}
	
	@Override
	public void send(String mess,SendContinuation continuation) throws IOException {
		processor.printSendMessage(mess);
		
		rawdataSend(mess.getBytes(),continuation);
	}
	
	@Override
	public void receive(ReceiveComplete handler) {
		receiveBuffer.clear();
		
		readPending = true;
		tcpSocket.read(receiveBuffer,config.timeout,TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer, Void>() {

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
				//check if exception is timeout
				if (exc instanceof InterruptedByTimeoutException)
					crash(new IOException("Read timed out"));
				else
					crash((Exception)exc);
			}
		});
	}
	
	@Override
	public void rawdataSend(byte[] bytes,SendContinuation continuation) {
		writeBuffer.clear();
		writeBuffer.put(bytes);
		writeBuffer.flip();
		
		if (isJoined())
			getJoiner().lockRead();
		
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
					
					unlockRead();
					
					if (getJoiner() != null)
						getJoiner().unlockRead();
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
	public String getIP() {
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
