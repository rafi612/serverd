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
		CommandProcessor processor = (CommandProcessor) testClient.getProcessor();
		processor.unjoin();
		
		assertEquals(testClient.getSend()[0], "ERROR Not joined");
	}

	@Test
	void executeTest() throws Exception {
		byte[] testBytes = {100,99,123,10};
		
		//inserting bytes to receive queue
		testClient.insertRawdataReceive(testBytes);

		CommandProcessor processor = (CommandProcessor) testClient.getProcessor();
		processor.join(testClient2.getId());
		
		//executing command
		executeTest(
				rawdataCommand,
				args(String.valueOf(testBytes.length)),
				testClient);
		
		assertEquals(testClient2.getRawdataSend().get(0), testBytes);
	}
}