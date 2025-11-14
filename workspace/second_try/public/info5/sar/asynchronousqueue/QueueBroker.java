package info5.sar.asynchronousqueue;

public abstract class QueueBroker {

	interface AcceptListener {
		void accepted(MessageQueue queue);
	}

	abstract boolean bind(int port, AcceptListener listener);

	abstract boolean unbind(int port);

	interface ConnectListener {

		void connected(MessageQueue queue);

		void refused();
	}

	abstract boolean connect(String name, int port, ConnectListener listener);
}