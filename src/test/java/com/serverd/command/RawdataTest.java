package com.serverd.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RawdataTest extends DoubleClientCommandTestCase {
	Rawdata rawdataCommand = new Rawdata();
	
	@Test
	void executeTest_NotJoined() throws Exception {
		executeTest(
				rawdataCommand,
				args("100"),
				testClient);
		testClient.unjoin();
		
		assertEquals(testClient.getSend()[0], "ERROR Not joined");
	}

	@Test
	void executeTest() throws Exception {
		byte[] testBytes = {100,99,123,10};
		testClient.join(testClient2.getID());
		
		//inserting bytes to receive queue
		testClient.insertRawdataReceive(testBytes);
		
		//executing command
		executeTest(
				rawdataCommand,
				args(String.valueOf(testBytes.length)),
				testClient);
		
		assertEquals(testClient2.getRawdataSend().get(0), testBytes);
	}
}