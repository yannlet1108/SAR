package info5.sar.asynchronousqueue;

public abstract class EventPump extends Thread {

	public abstract void post(Runnable e);

	public abstract void post(Runnable e, int delay);
	
	public abstract void run();
	
	public abstract void shutdown();

	public abstract int getNumberPendingEvents();

}
