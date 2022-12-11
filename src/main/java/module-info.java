/**
 * ServerD server and plugins framework module
 */
module serverd 
{
	exports com.serverd.util;
	exports com.serverd.log;
	exports com.serverd.client;
	exports com.serverd.plugin.listener;
	exports com.serverd.main;
	exports com.serverd.command;
	exports com.serverd.plugin;
	
	opens com.serverd.util;
	opens com.serverd.log;
	opens com.serverd.client;
	opens com.serverd.plugin.listener;
	opens com.serverd.main;
	opens com.serverd.command;
	opens com.serverd.plugin;
}