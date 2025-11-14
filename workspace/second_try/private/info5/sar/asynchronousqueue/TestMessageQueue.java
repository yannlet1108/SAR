package info5.sar.asynchronousqueue;

import info5.sar.asynchronousqueue.QueueBroker.AcceptListener;
import info5.sar.asynchronousqueue.QueueBroker.ConnectListener;

public class TestMessageQueue {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EventPump pump = CEventPump.getInstance();
		QueueBroker qb1 = new CQueueBroker("b1");
		Runnable bootstrap1 = new Runnable() {
			@Override
			public void run() {
				qb1.bind(8080, new AcceptListener() {
					@Override
					public void accepted(MessageQueue queue) {
						System.out.println("Connection accepted");
					}

				});

			}

		};
		Runnable bootstrap2 = new Runnable() {
			@Override
			public void run() {
				qb1.connect("b1", 8080, new ConnectListener() {

					@Override
					public void connected(MessageQueue queue) {
						System.out.println("Connection accepted");
						String msg = "Coucou";
						byte[] bytes = msg.getBytes();
						queue.send(bytes);
					}

					@Override
					public void refused() {
						// TODO Auto-generated method stub
					}
				});
			}
		};
		pump.post(bootstrap1);
		pump.post(bootstrap2);
		pump.start();

		synchronized (pump) {
			try {
				pump.wait(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		((CEventPump) pump).shutdown();
	}
}