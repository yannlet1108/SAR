package info5.sar.channels;

// Code inspired from Yohan's work
// Adapted to my implementation and choices

public class TestChannel {

	public static void main(String[] args) {

		Task.VERBOSE = true; // set to true to see per-test prints

		if (Task.VERBOSE)
			Task.log("Test1...");
		if (!test1()) {
			System.err.println("Test 1 failed.");
			System.exit(1);
		}
		if (Task.VERBOSE)
			Task.log("Test2...");
		if (!test2()) {
			System.err.println("Test 2 failed.");
			System.exit(1);
		}
		if (Task.VERBOSE)
			Task.log("Test3...");
		if (!test3()) {
			System.err.println("Test 3 failed.");
			System.exit(1);
		}
		if (Task.VERBOSE)
			Task.log("Test4...");
		if (!test4()) {
			System.err.println("Test 4 failed.");
			System.exit(1);
		}
		if (Task.VERBOSE)
			Task.log("Test5...");
		if (!test5()) {
			System.err.println("Test 5 failed.");
			System.exit(1);
		}
		if (Task.VERBOSE)
			Task.log("Test6...");
		if (!test6()) {
			System.err.println("Test 6 failed.");
			System.exit(1);
		}
		if (Task.VERBOSE)
			Task.log("Test7...");
		if (!test7()) {
			System.err.println("Test 7 failed.");
			System.exit(1);
		}
		if (Task.VERBOSE)
			Task.log("Test8...");
		if (!test8()) {
			System.err.println("Test 8 failed.");
			System.exit(1);
		}
		if (Task.VERBOSE)
			Task.log("Test9...");
		if (!test9()) {
			System.err.println("Test 9 failed.");
			System.exit(1);
		}
		if (Task.VERBOSE)
			Task.log("Test10...");
		if (!test10()) {
			System.err.println("Test 10 failed.");
			System.exit(1);
		}
		if (Task.VERBOSE)
			Task.log("Test11...");
		if (!test11()) {
			System.err.println("Test 11 failed.");
			System.exit(1);
		}

		System.out.println("All tests passed.");

	}

	// Each broker must have a unique name
	private static boolean test1() {
		CBroker brokerA = new CBroker("BrokerA");
		try {
			CBroker brokerB = new CBroker("BrokerA");
			assert false : "Expected exception for duplicate broker name not thrown.";
		} catch (IllegalArgumentException e) {
			// Expected exception
		}
		return true;
	}

	// Lookup should return the correct broker
	private static boolean test2() {
		CBroker brokerC = new CBroker("BrokerC");
		CBroker foundBroker = (CBroker) BrokerManager.getInstance().getBroker("BrokerC");
		assert foundBroker != null && foundBroker.getName().equals("BrokerC") : "Broker lookup failed.";
		return true;
	}

	// Connection to a non-existing broker should return null
	private static boolean test3() {
		CBroker broker = new CBroker("Broker");
		Channel shouldBeNull = broker.connect("NonExistentBroker", 1024);
		return shouldBeNull == null;
	}

	// Connection and disconnection general usecase inside the same broker
	private static boolean test4() {
		CBroker broker1;

		try {
			BrokerManager.getInstance().getBroker("Broker1");
			throw new RuntimeException("This part shoulb be inaccessible");
		} catch (IllegalArgumentException e) {
			// Broker not found (as expected), we create it
		}
		broker1 = new CBroker("Broker1");

		Task task1 = new Task("Task1", broker1);
		Task task2 = new Task("Task2", broker1);

		task1.start(new Runnable() {
			@Override
			public void run() {

				Channel channel = broker1.connect("NonExistentBroker", 1212);
				if (channel != null) {
					System.err.println("ERR: task1: channel should be null when connecting to non-existent broker");
					return;
				}

				channel = broker1.connect("Broker1", 8080);
				if (channel == null) {
					System.err.println("ERR: task1: channel is null");
					return;
				}

				if (channel.disconnected()) {
					System.err.println("ERR: task1: channel should not be disconnected");
				}
				channel.disconnect();
				if (channel.disconnected()) {
					// System.out.println("task1: channel is now disconnected");
				} else {
					System.err.println("ERR: task1: channel should be disconnected but is not");
				}
			}

		});

		task2.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.accept(8080);
				if (channel == null) {
					System.err.println("ERR: task2: channel is null");
					return;
				}
				while (!channel.disconnected()) {
					// wait for disconnection
				}
			}
		});

		try {
			task1.join();
			task2.join();
		} catch (InterruptedException e) {
			System.err.println("ERR: Task interrupted: " + e.getMessage());
			return false;
		}
		return true;
	}

	// Double accept on same port should fail
	private static boolean test5() {
		if (!BrokerManager.getInstance().isBrokerExisting("Broker1")) {
			CBroker broker1 = new CBroker("Broker1");
		}
		CBroker broker1 = (CBroker) BrokerManager.getInstance().getBroker("Broker1");

		Task task1 = new Task("Task1", broker1);
		Task task2 = new Task("Task2", broker1);

		task1.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.accept(9090);
				if (channel == null) {
					System.err.println("ERR: task1: channel is null");
					return;
				}
				// System.out.println("task1 accepted -> " + channel.getRemoteName());
			}
		});

		task2.start(new Runnable() {
			@Override
			public void run() {
				try {
					// Give task1 time to start the accept
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Do nothing
				}
				try {
					broker1.accept(9090);
					System.err.println("ERR: task2: expected exception for double accept not thrown.");
				} catch (IllegalArgumentException e) {
					// Expected exception
					// System.out.println("Expected exception for double accept caught.");
				}

				Channel channel = broker1.connect("Broker1", 9090);

				// System.out.println("task2 connected -> " + channel.getRemoteName());
			}
		});
		try {
			task1.join();
			task2.join();
		} catch (InterruptedException e) {
			System.err.println("ERR: Task interrupted: " + e.getMessage());
			return false;
		}
		return true;
	}

	// Multiple connections on same port should work
	private static boolean test6() {
		if (!BrokerManager.getInstance().isBrokerExisting("Broker1")) {
			CBroker broker1 = new CBroker("Broker1");
		}
		CBroker broker1 = (CBroker) BrokerManager.getInstance().getBroker("Broker1");

		Task task1 = new Task("Task1", broker1);
		Task task2 = new Task("Task2", broker1);
		Task task3 = new Task("Task3", broker1);

		task1.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.connect("Broker1", 8080);
				if (channel == null) {
					System.err.println("ERR: task1: channel is null");
					return;
				}
			}
		});

		task2.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.connect("Broker1", 8080);
				if (channel == null) {
					System.err.println("ERR: task2: channel is null");
					return;
				}
			}
		});

		task3.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.accept(8080);
				Channel channel2 = broker1.accept(8080);
				if (channel == null || channel2 == null) {
					System.err.println("ERR: task3: one of the channels is null");
					return;
				}
			}
		});

		try {
			task1.join();
			task2.join();
			task3.join();
		} catch (InterruptedException e) {
			System.err.println("ERR: Task interrupted: " + e.getMessage());
			return false;
		}
		return true;
	}

	// Connection between two different brokers should work
	private static boolean test7() {
		if (!BrokerManager.getInstance().isBrokerExisting("Broker1")) {
			CBroker broker1 = new CBroker("Broker1");
		}
		CBroker broker1 = (CBroker) BrokerManager.getInstance().getBroker("Broker1");

		if (!BrokerManager.getInstance().isBrokerExisting("Broker2")) {
			CBroker broker2 = new CBroker("Broker2");
		}
		CBroker broker2 = (CBroker) BrokerManager.getInstance().getBroker("Broker2");

		Task task1 = new Task("Task1", broker1);
		Task task2 = new Task("Task2", broker2);
		
//		broker1 = (CBroker) task1.getBroker();

		task1.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.connect("Broker2", 8080);
				if (channel == null) {
					System.err.println("ERR: task1: channel is null");
					return;
				}
			}
		});

		task2.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker2.accept(8080);
				if (channel == null) {
					System.err.println("ERR: task2: channel is null");
					return;
				}
			}
		});

		try {
			task1.join();
			task2.join();
		} catch (InterruptedException e) {
			System.err.println("ERR: Task interrupted: " + e.getMessage());
			return false;
		}
		return true;
	}

	// Message exchange general usecase test
	private static boolean test8() {
		if (!BrokerManager.getInstance().isBrokerExisting("Broker1")) {
			CBroker broker1 = new CBroker("Broker1");
		}
		CBroker broker1 = (CBroker) BrokerManager.getInstance().getBroker("Broker1");

		Task task1 = new Task("Task1", broker1);
		Task task2 = new Task("Task2", broker1);

		task1.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.connect("Broker1", 8080);
				if (channel == null) {
					System.err.println("ERR: task1: channel is null");
					return;
				}
				// System.out.println("task1 connected -> " + channel.getRemoteName());

				String message = "Hello from task1";
				try {
					channel.write(message.getBytes(), 5, message.length());
					System.err.println("ERR: task1: write with invalid offset should have failed but didn't");
				} catch (IllegalArgumentException e) {
					// expected exception
				}

				channel.write(message.getBytes(), 0, message.length());

				byte[] buffer = new byte[1024];
				try {
					int bytesRead = channel.read(buffer, 5, buffer.length);
					System.err.println("ERR: task1: read with invalid offset should have failed but didn't");
				} catch (IllegalArgumentException e) {
					// expected exception
				}

				int bytesRead = channel.read(buffer, 0, buffer.length);
				String receivedMessage = new String(buffer, 0, bytesRead);
				if (!receivedMessage.equals("Hello from task2")) {
					System.err.println("ERR: task1: received incorrect message: " + receivedMessage);
				} else {
//					 System.out.println("task1: received message: " + receivedMessage);
				}

				channel.disconnect();
			}
		});

		task2.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.accept(8080);
				if (channel == null) {
					System.err.println("task2: channel is null");
					return;
				}
				// System.out.println("task2 accepted -> " + channel.getRemoteName());

				byte[] buffer = new byte[1024];

				int bytesRead = channel.read(buffer, 0, buffer.length);
				String receivedMessage = new String(buffer, 0, bytesRead);
				if (!receivedMessage.equals("Hello from task1")) {
					System.err.println("ERR: task2: received incorrect message: " + receivedMessage);
				} else {
					// System.out.println("task2: received message: " + receivedMessage);
				}

				String message = "Hello from task2";
				channel.write(message.getBytes(), 0, message.length());

				while (!channel.disconnected()) {
					// wait for disconnection
				}
				try {
					channel.write(new byte[] { 1, 2, 3 }, 0, 3);
					System.err.println("ERR: task2: write should have failed on disconnected channel but didn't");
				} catch (IllegalStateException e) {
					// Expected exception
				}

				try {
					channel.read(new byte[3], 0, 3);
					System.err.println("ERR: task2: read should have failed on disconnected channel but didn't");
				} catch (IllegalStateException e) {
					// Expected exception
				}
			}
		});

		try {
			task1.join();
			task2.join();
		} catch (InterruptedException e) {
			System.err.println("ERR: Task interrupted: " + e.getMessage());
			return false;
		}
		return true;
	}

	// Read and write blocking behavior test
	private static boolean test9() {
		Broker broker1;
		if (BrokerManager.getInstance().getBroker("Broker1") == null) {
			broker1 = new CBroker("Broker1");
		} else {
			broker1 = (CBroker) BrokerManager.getInstance().getBroker("Broker1");
		}

		Task task1 = new Task("Task1", broker1);
		Task task2 = new Task("Task2", broker1);

		task1.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.connect("Broker1", 8080);
				byte[] buffer = new byte[10];
				int bytesRead = channel.read(buffer, 0, 10);
				String receivedMessage = new String(buffer, 0, bytesRead);
				if (!receivedMessage.equals("Hi")) {
					System.err.println("ERR: task1: received incorrect message: " + receivedMessage);
				}

				String longMessage = "Hello, this is a message longer than the buffer size to test blocking write behavior.";
				int bytesWritten = channel.write(longMessage.getBytes(), 0, longMessage.length());
				if (bytesWritten != ( ((CChannel)channel).getBufferMaxSize()) -1)  {
					System.err.println("ERR: task1: expected to write 64 bytes, but wrote " + bytesWritten + " bytes.");
				}

				String extraMessage = "Extra";
				bytesWritten = channel.write(extraMessage.getBytes(), 0, extraMessage.length());
				if (bytesWritten != 3) {
					System.err.println("ERR: task1: expected to write 5 bytes, but wrote " + bytesWritten + " bytes.");
				}
			}

		});

		task2.start(new Runnable() {

			@Override
			public void run() {
				Channel channel = broker1.accept(8080);
				try {
					Thread.sleep(50); // Give task1 time to start and block on read
				} catch (InterruptedException e) {
					// Do nothing
				}
				String smallmessage = "Hi";
				channel.write(smallmessage.getBytes(), 0, smallmessage.length());

				try {
					Thread.sleep(50); // Give task1 time to write the long message and block on extra write
				} catch (InterruptedException e) {
					// Do nothing
				}
				byte[] buffer = new byte[100];
				int bytesRead = channel.read(buffer, 0, 3);
			}
		});

		try {
			task1.join();
			task2.join();
		} catch (InterruptedException e) {
			System.err.println("ERR: Task interrupted: " + e.getMessage());
			return false;
		}
		return true;
	}

	// Disconnection while blocked on reading comportment test
	private static boolean test10() {
		Broker broker1;
		if (BrokerManager.getInstance().getBroker("Broker1") == null) {
			broker1 = new CBroker("Broker1");
		} else {
			broker1 = (CBroker) BrokerManager.getInstance().getBroker("Broker1");
		}

		Task task1 = new Task("Task1", broker1);
		Task task2 = new Task("Task2", broker1);

		task1.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.connect("Broker1", 8080);

				byte[] buffer = new byte[10];
				int bytesRead = channel.read(buffer, 0, 10);
				if (bytesRead != 0) {
					System.err
							.println("ERR: task1: expected 0 bytes read on disconnected channel, but got " + bytesRead);
				}

			}

		});

		task2.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.accept(8080);
				try {
					Thread.sleep(50); // Give task1 time to start and block on read
				} catch (InterruptedException e) {
					// Do nothing
				}
				channel.disconnect();
			}
		});
		return true;
	}

	// Disconnection while blocked on writing comportment test
	private static boolean test11() {
		Broker broker1;
		if (BrokerManager.getInstance().getBroker("Broker1") == null) {
			broker1 = new CBroker("Broker1");
		} else {
			broker1 = (CBroker) BrokerManager.getInstance().getBroker("Broker1");
		}

		Task task1 = new Task("Task1", broker1);
		Task task2 = new Task("Task2", broker1);

		task1.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.connect("Broker1", 8080);

				String longmessage = "Hello, this is a message longer than the buffer size to test blocking write behavior.";
				int byteswritten = channel.write(longmessage.getBytes(), 0, longmessage.length());

				String extraMessage = "Extra";
				byteswritten = channel.write(extraMessage.getBytes(), 0, extraMessage.length());
				if (byteswritten != 0) {
					System.err.println("ERR: task1: expected 0 bytes written on disconnected channel, but wrote "
							+ byteswritten + " bytes.");
				}
			}
		});

		task2.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker1.accept(8080);
				try {
					Thread.sleep(50); // Give task1 time to start and block on write
				} catch (InterruptedException e) {
					// Do nothing
				}
				channel.disconnect();
			}
		});

		try {
			task1.join();
			task2.join();
		} catch (InterruptedException e) {
			System.err.println("ERR: Task interrupted: " + e.getMessage());
			return false;
		}
		return true;
	}
}