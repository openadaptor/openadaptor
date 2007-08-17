package org.openadaptor.thirdparty.mq;

import junit.framework.TestCase;

public class MqConnectionTestCase extends TestCase {
	private static final String MQ_CHANNEL_NAME = "CH_ETXJNL_01";
	private static final String MQ_MANAGER = "MQS1";
	private static final String MQ_QUEUE = "DEALBUS_TEST";
	private static final String MQ_HOST = "barney";
	private static final int MQ_PORT = 1416;
	private static final String MQ_USER = "KSDJOB";
	private static final String MQ_PASSWORD = "MQS1ETX";
	
	private static final String TEST_MESSAGE_CONTENT = "this is a test";
	
	private MqConnection connection;
	
	public void setUp() throws Exception {
		connection = new MqConnection();
		connection.setChannelName(MQ_CHANNEL_NAME);
		connection.setHostName(MQ_HOST);
		connection.setManagerName(MQ_MANAGER);
		connection.setPassword(MQ_PASSWORD);
		connection.setUserName(MQ_USER);
		connection.setPort(MQ_PORT);
		connection.setQueueName(MQ_QUEUE);
	}
	
	public void tearDown() {
		//connection.close();
	}
	
	public void testMqConnectionForRead() {
		MqReadConnector reader = new MqReadConnector();
		reader.setId("Reader");
		reader.setConnection(connection);
		reader.connect();
		assertTrue(reader.getConnection() != null);
		reader.disconnect();
	}
	
	public void testMqConnectionForWrite() {
		MqWriteConnector writer = new MqWriteConnector();
		writer.setId("Writer");
		writer.setConnection(connection);
		writer.connect();
		assertTrue(writer.getConnection() != null);
		writer.disconnect();
	}
	
	public void testReaderWithTimeout() {
		MqReadConnector reader = new MqReadConnector();
		reader.setId("Reader");
		reader.setConnection(connection);
		reader.connect();
		assertTrue(reader.getConnection() != null);
		long now = System.currentTimeMillis();
		Object[] result = reader.next(500);
		long diff = System.currentTimeMillis() - now;
		if (result == null) {
			assertTrue(diff > 500);
			assertTrue(diff < 600);
		} else {
			fail("Expected to receive nothing on the queue but got " + result);
		}
		reader.disconnect();
	}
	
	public void testReaderWithoutTimeout1() {
		MqReadConnector reader = new MqReadConnector();
		reader.setId("Reader");
		reader.setConnection(connection);
		reader.connect();
		assertTrue(reader.getConnection() != null);
		long now = System.currentTimeMillis();
		Object[] result = reader.next(0);
		long diff = System.currentTimeMillis() - now;
		if (result == null) {
			assertTrue(diff < 100);
		} else {
			fail("Expected to receive nothing on the queue but got " + result);
		}
		reader.disconnect();
	}
	
	public void testReaderWithoutTimeout2() {
		MqReadConnector reader = new MqReadConnector();
		reader.setId("Reader");
		reader.setConnection(connection);
		reader.connect();
		assertTrue(reader.getConnection() != null);
		long now = System.currentTimeMillis();
		Object[] result = reader.next(-40);
		long diff = System.currentTimeMillis() - now;
		if (result == null) {
			assertTrue(diff < 100);
		} else {
			fail("Expected to receive nothing on the queue but got " + result);
		}
		reader.disconnect();
	}
	
	public void testReaderWithInvalidTimeout() {
		MqReadConnector reader = new MqReadConnector();
		reader.setId("Fail Reader");
		reader.setConnection(connection);
		reader.connect();
		assertTrue(reader.getConnection() != null);
		try {
			long timeout = (long)Integer.MAX_VALUE + 1;
			reader.next(timeout);
			fail("Reader should reject wait timeout greater than Integer.MAX_VALUE");
		} catch (IllegalArgumentException e) {
			// expected
		} finally {
			reader.disconnect();
		}
	}
	
	public void testQueueAndReceiveMessage() {
		MqWriteConnector writer = new MqWriteConnector();
		writer.setId("Writer");
		writer.setConnection(connection);
		writer.connect();
		assertTrue(writer.getConnection() != null);
		writer.deliver(new String[] { TEST_MESSAGE_CONTENT });
		writer.disconnect();

		MqReadConnector reader = new MqReadConnector();
		reader.setId("Reader");
		reader.setConnection(connection);
		reader.connect();
		assertTrue(reader.getConnection() != null);
		Object[] result = reader.next(500);
		assertTrue(result != null && result.length == 1 && result[0].equals(TEST_MESSAGE_CONTENT));
		reader.disconnect();
	}
}