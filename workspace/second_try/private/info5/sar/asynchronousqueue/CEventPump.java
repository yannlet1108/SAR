package info5.sar.asynchronousqueue;

import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;

public class CEventPump extends EventPump {

	private boolean isRunning;
	private volatile Queue<Event> events;

	private static CEventPump instance;

	private CEventPump() {
		isRunning = true;
		events = new PriorityQueue<Event>();
	}

	public static CEventPump getInstance() {
		if (instance == null) {
			instance = new CEventPump();
		}
		return instance;
	}

	public void start() {
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {

		while (isRunning) {

			synchronized (events) {
				while (!events.isEmpty()) {
					CEvent event = (CEvent) events.peek();
					Date currentTime = new Date(System.currentTimeMillis());
					if (event.getTimeToExecute().before(currentTime)) {
						events.remove();
						event.react();
					}
				}
			}
		}
	}

	@Override
	synchronized public void post(Runnable r) {
		Event event = new CEvent(r);
		events.add(event);
	}

	@Override
	synchronized public void post(Runnable r, int delay) {
		Event event = new CEvent(r, delay);
		events.add(event);
	}

	public void shutdown() {
		isRunning = false;
	}

	public int getNumberPendingEvents() {
		return events.size();
	}

}