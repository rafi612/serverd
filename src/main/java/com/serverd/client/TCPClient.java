package com.serverd.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

import com.serverd.config.Config;

/**
 * TCP client class
 */
public class TCPClient extends AsyncClient {	
	
	/** Socket*/
	protected AsynchronousSocketChannel tcpSocket;
	protected Config config;
	
	/**
	 * TCPClient class constructor
	 * @param id Client's ID
	 * @param socket Socket instance
	 * @throws IOException when InputStream or OutputStream throws {@link IOException}
	 */
	public TCPClient(int id,AsynchronousSocketChannel socket,Config config) throws IOException {
		super(id);
		this.config = config;
		
		protocol = Protocol.TCP;
		tcpSocket = socket;
	}

	
	@Override
	public void send(String mess) throws IOException {
		log.info("<Sended> " + mess);

		rawdataSend(encoder.encode(mess, this).getBytes());
	}
	
	public void receive(ReceiveComplete handler) {
		if (locked)
			return;
		
		receiveBuffer.clear();
		
		readPending = true;
		tcpSocket.read(receiveBuffer,config.timeout,TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer, Void>() {

			@Override
			public void completed(Integer len, Void attachment) {
				readPending = false;
				
				receiveBuffer.flip();
				if (len == -1) {
					crash(new IOException("Connection closed"));
					return;
				}
				
				byte[] ret = new byte[len];
				receiveBuffer.get(ret, 0, len);
				
				handler.receiveDone(ret);	
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				readPending = false;
				crash((Exception)exc);
			}
		});
	}
	
	@Override
	public void rawdataSend(byte[] bytes) throws IOException {
		queueBuffer(bytes);
		
		
	}
	
	public void processSend(ByteBuffer buffer) {
		
		log.debug("Process send");
		
		tcpSocket.write(buffer, null, new CompletionHandler<Integer, Void>() {
			@Override
			public void completed(Integer bytesWritten, Void attachment) {
				
				if (buffer.hasRemaining()) {
					tcpSocket.write(writeBuffer, null, this);
					
					buffer.clear();
                } else {
					if (isJoined())
						getJoiner().unlockRead();
					
					invokeReceive();
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
			return ((InetSocketAddress) tcpSocket.getRemoteAddress()).getAddress().toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public int getPort() {
		try {
			return ((InetSocketAddress) tcpSocket.getRemoteAddress()).getPort();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
