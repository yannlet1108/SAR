package info5.sar.asynchronousqueue;

import java.util.Date;

public interface Event {

	public void react();
	
	public Date getTimeToExecute();
	
	public int compareTo(CEvent otherEvent);

}