package com.serverd.plugin.listener;

import com.serverd.client.Client;

public interface ConnectListener
{
	public void onConnect(Client client);
	public void onDisconnect(Client client);
}
