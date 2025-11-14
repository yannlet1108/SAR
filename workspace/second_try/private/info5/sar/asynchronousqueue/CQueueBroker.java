package info5.sar.asynchronousqueue;

import java.util.HashMap;
import java.util.Map;

import info5.sar.channels.CBroker;

import info5.sar.channels.Broker;
import info5.sar.channels.Channel;
import info5.sar.channels.Task;

public class CQueueBroker extends QueueBroker {

	public class CAcceptListener implements AcceptListener {

		@Override
		public void accepted(MessageQueue queue) {
			System.out.println("Accepted connection on message queue: " + queue);
		}

	}

	public class CConnectListener implements ConnectListener {

		@Override
		public void connected(MessageQueue queue) {
			System.out.println("Connected to message queue: " + queue);
		}

		@Override
		public void refused() {
			System.out.println("Connection to message queue refused");
		}
	}

	private final Map<Integer, Task> pendingBinds;
	private CEventPump eventPump;
	private Broker broker;

	CQueueBroker(String name) {
		this.broker = new CBroker(name);
		pendingBinds = new HashMap<>();
		eventPump = CEventPump.getInstance();
	}

	@Override
	boolean bind(int port, AcceptListener listener) {

		synchronized (pendingBinds) {

			Task task = pendingBinds.get(port);

			if (task != null) {
				// a bind is already pending on this port
				return false;
			}
			task = new Task("bind", broker);
			pendingBinds.put(port, task);

			task.start(new Runnable() {
				@Override
				public void run() {
					while (true) {
						Channel channel = broker.accept(port);
						MessageQueue messageQueue = new CMessageQueue(channel);
						eventPump.post(() -> listener.accepted(messageQueue));
					}
				}
			});
		}
		return true;
	}

	@Override
	boolean unbind(int port) {

		Task task = pendingBinds.get(port);

		if (task == null) {
			// no bind pending on this port
			return false;
		}

		task.interrupt();
		((CBroker) broker).removeAccept(port);
		return true;

	}

	@Override
	boolean connect(String name, int port, ConnectListener listener) {
		Task task = new Task("connect", broker);

		task.start(new Runnable() {
			@Override
			public void run() {
				Channel channel = broker.connect(name, port);
				MessageQueue messageQueue = new CMessageQueue(channel);
				eventPump.post(() -> listener.connected(messageQueue));
			}
		});

		return true;
	}

}
