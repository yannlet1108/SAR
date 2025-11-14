package info5.sar.asynchronousqueue;

abstract class MessageQueue {

	interface Listener {

		void received(byte[] msg);

		void closed();
	}

	abstract void setListener(Listener l);

	abstract boolean send(byte[] bytes);

	abstract boolean send(byte[] bytes, int offset, int length);

	abstract void close();

	abstract boolean closed();
}