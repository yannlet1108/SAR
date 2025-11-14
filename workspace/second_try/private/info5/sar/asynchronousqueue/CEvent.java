package info5.sar.asynchronousqueue;

import java.util.Date;

public class CEvent implements Event, Comparable<CEvent>{

	private Runnable runnable;
	private int delay;
	private Date timeToExecute;

	public CEvent(Runnable runnable, int delay) {
		this.runnable = runnable;
		this.delay = delay;
		timeToExecute = new Date(System.currentTimeMillis() + delay);
	}

	public CEvent(Runnable runnable) {
		this(runnable, 0);
		timeToExecute = new Date(System.currentTimeMillis());
	}

	@Override
	public void react() {
		runnable.run();
	}

	public Date getTimeToExecute() {
		return timeToExecute;
	}

	@Override
	public int compareTo(CEvent otherEvent) {
		return getTimeToExecute().compareTo(otherEvent.getTimeToExecute());
	}


}
