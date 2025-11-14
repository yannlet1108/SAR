package info5.sar.asynchronousqueue;

import info5.sar.channels.Channel;
import info5.sar.channels.Task;

public class CMessageQueue extends MessageQueue {

	public class CListener implements Listener {

		@Override
		public void received(byte[] msg) {
			System.out.println(msg);
		}

		@Override
		public void closed() {
			System.out.println("This Message Queue is disconnected");
		}

	}

	private Channel channel;
	private Listener listener;

	private Task receiveTask;
	private Task sendTask;

	public CMessageQueue(Channel channel) {
		this.channel = channel;
		receiveTask = new Task("Receive task", null);
		receiveTask.start(buildReadRunnable());
//		sendTask = new Task("Send task", null);
		setListener(new Listener() {
			@Override
			public void received(byte[] msg) {
				System.out.println("Message bien reçu:" + new String(msg));
			}

			@Override
			public void closed() {
			}
		});
	}

	@Override
	void setListener(Listener l) {
		synchronized (this) {
			this.listener = l;
			this.notifyAll();
		}
	}

	@Override
	boolean send(byte[] bytes) {
		if (closed() || listener == null) {
			return false;
		}
		sendTask = new Task("Send task", null);
		sendTask.start(writeRunnable(bytes, 0, bytes.length));
		return true;
	}

	@Override
	boolean send(byte[] bytes, int offset, int length) {
		if (closed() || listener == null) {
			return false;
		}
		sendTask = new Task("Send task", null);
		sendTask.start(writeRunnable(bytes, 0, bytes.length));
		return true;
	}

	@Override
	void close() {
		channel.disconnect();
	}

	@Override
	boolean closed() {
		return channel.disconnected();
	}

	private Runnable writeRunnable(byte[] bytes, int offset, int length) {
		return new Runnable() {
			@Override
			public void run() {
				writeMessage(bytes, offset, length);
			}

		};
	}

	private boolean writeMessage(byte[] bytes, int offset, int length) {
		synchronized (channel) {
			byte[] lengthToWrite = intToBytes(length);
			// write the 4 bytes of length
			int off = 0;
			int remaining = 4;
			while (remaining > 0) {
				int written = channel.write(lengthToWrite, off, remaining);
				if (written <= 0) {
					// channel closed or no progress
					if (channel.disconnected())
						return false;
					continue;
				}
				off += written;
				remaining -= written;
			}

			// write the message payload (may require several write calls)
			off = offset;
			remaining = length;
			while (remaining > 0) {
				int written = channel.write(bytes, off, remaining);
				if (written <= 0) {
					if (channel.disconnected())
						return false;
					continue;
				}
				off += written;
				remaining -= written;
			}
			return true;
		}
	}

	private byte[] intToBytes(int value) {
		return new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
	}

	private int bytesToInt(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
	}

	private Runnable buildReadRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				synchronized (channel) {
	                while (!closed()) {
	                    int numberOfBytesRead = 0;
	                    byte[] lengthBuffer = new byte[4];
	                    while (numberOfBytesRead < 4) {
	                        int bytesRead = channel.read(lengthBuffer, numberOfBytesRead, 4 - numberOfBytesRead);
	                        numberOfBytesRead += bytesRead;
	                    }

	                    int messageLength = bytesToInt(lengthBuffer);
	                    byte[] message = new byte[messageLength];
	                    numberOfBytesRead = 0;
	                    while (numberOfBytesRead < messageLength) {
	                        int bytesRead = channel.read(message, numberOfBytesRead, messageLength - numberOfBytesRead);
	                        numberOfBytesRead += bytesRead;
	                    }
	                    listener.received(message);
	                }
	            }
			}
		};
	}

}
