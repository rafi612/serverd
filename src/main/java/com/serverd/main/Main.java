package com.serverd.main;

import com.serverd.app.ServerdApplication;

public class Main {
	public static void main(String[] args) {
		try {
			ServerdApplication app = new ServerdApplication();
			app.parseCmdArgs(args);
			app.run();
		} catch (RuntimeException e) {
			System.exit(-1);
		}
	}
}
