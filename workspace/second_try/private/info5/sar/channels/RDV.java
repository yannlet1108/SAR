package info5.sar.channels;

import java.util.concurrent.Semaphore;

// Base of the code was written for the PC's mini-projet in 4th year
public class RDV {

	private Semaphore rdv = new Semaphore(0);
	private int nexpected;
	private int narrived = 0;

	private Semaphore mutex = new Semaphore(1);
	
	private PendingOperation pendingOperation;
	
	Channel firstEndpoint;

	RDV(int nexpected, PendingOperation pendingOperation) {
		this.nexpected = nexpected;
		this.pendingOperation = pendingOperation;
	}

	Channel come(Broker broker, int port) {
		try {
			mutex.acquire();
			narrived++;
			if (narrived < nexpected) {
				
				firstEndpoint = new CChannel(broker, port);
				
				mutex.release();
				rdv.acquire();
				
				return firstEndpoint;
			} else {
				
				Channel secondEndpoint = new CChannel(broker, port);
				secondEndpoint.setPeer(firstEndpoint);
				firstEndpoint.setPeer(secondEndpoint);
				
				rdv.release(nexpected - 1);
				mutex.release();
				
				return secondEndpoint;				
			}
		} catch (InterruptedException e) {
			// Happens when unbind() is called
			return null;
		}
	}
	
	PendingOperation getPendingOperation() {
		return pendingOperation;
	}

}