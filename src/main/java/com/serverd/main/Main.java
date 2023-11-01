package com.serverd.main;

import com.serverd.app.ServerdApplication;

public class Main {
	public static final String VERSION = "v1.0.0";
	
	public static void main(String[] args) {
		ServerdApplication app = new ServerdApplication();
		app.parseCmdArgs(args);
		app.run();
	}
}
