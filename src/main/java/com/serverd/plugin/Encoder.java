package com.serverd.plugin;

import com.serverd.client.Client;

/**
 * Encoder can be used to encrypt and decrypt messages. Encoder can be assigned to client.
 */
public class Encoder 
{
	/**
	 * Encode message
	 * @param message Message to encode
	 * @param client Client instance on encoder is executed
	 * @return Encoded message
	 */
	public String encode(String message,Client client)
	{
		return message;
	}
	
	/**
	 * Decode message
	 * @param message Message to decode
	 * @param client Client instance on encoder is executed
	 * @return Decoded message
	 */
	public String decode(String message,Client client)
	{
		return message;
	}

}
