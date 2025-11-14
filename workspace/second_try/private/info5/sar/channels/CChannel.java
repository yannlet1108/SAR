/*
 * Copyright (C) 2023 Pr. Olivier Gruber                                    
 *                                                                       
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, either version 3 of the License, or     
 * (at your option) any later version.                                   
 *                                                                       
 * This program is distributed in the hope that it will be useful,       
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         
 * GNU General Public License for more details.                          
 *                                                                       
 * You should have received a copy of the GNU General Public License     
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package info5.sar.channels;

import info5.sar.utils.CircularBuffer;

public class CChannel extends Channel {

	private volatile boolean isConnected;

	private CChannel otherEndpoint;
	private CircularBuffer buffer;

	private static final int BUFFER_MAX_SIZE = 64;

	private int port; // added for helping debugging applications.

	protected CChannel(Broker broker, int port) {
		super(broker);
		isConnected = true;
		this.port = port;
		buffer = new CircularBuffer(BUFFER_MAX_SIZE);
	}

	// added for helping debugging applications.
	public String getRemoteName() {
		return "Channel from broker : " + broker.getName() + " connected on port : " + port;
	}

	@Override
	public int read(byte[] bytes, int offset, int length) {

		if (offset < 0 || length < 0 || offset + length > bytes.length)
			throw new IllegalArgumentException("Invalid offset or length");

		int read = 0;
		synchronized (this) {
			if (!isConnected)
				throw new IllegalStateException("The channel is disconnected");

			while (buffer.empty()) {

				if (!isConnected) {
					// Unblocked by disconnect: return 0
					return 0;
				}
				try {
					wait();
				} catch (InterruptedException ie) {
					// Restore interrupt status and continue to check conditions
					Thread.currentThread().interrupt();
				}
			}

			// Read up to "length" bytes or until buffer becomes empty
			while (read < length && !buffer.empty()) {
				bytes[offset + read] = buffer.pull();
				read++;
				notifyAll();
			}

		}

		return read;
	}

	@Override
	public int write(byte[] bytes, int offset, int length) {
		// writing on a disconnected channel is illegal
		if (!isConnected)
			throw new IllegalStateException("channel is disconnected");
		return otherEndpoint.receive(bytes, offset, length);
	}

	private int receive(byte[] bytes, int offset, int length) {

		if (offset < 0 || length < 0 || offset + length > bytes.length)
			throw new IllegalArgumentException("Invalid offset or length");

		int written = 0;

		synchronized (this) {

			while (buffer.full() && !disconnected()) {
				try {
					wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return written;
				}
			}
			if (disconnected()) {
				return written;
			}
			while (length > 0 && !buffer.full()) {
				buffer.push(bytes[offset + written]);
				written++;
				length--;
			}
			notifyAll();
		}
		return written;
	}

	@Override
	public void disconnect() {
		getDisconnected();
		otherEndpoint.getDisconnected();
	}

	private void getDisconnected() {
		isConnected = false;
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public boolean disconnected() {
		return !isConnected;
	}

	@Override
	public void setPeer(Channel otherEndpoint) {
		this.otherEndpoint = (CChannel)otherEndpoint;
	}

	@Override
	public int getBufferMaxSize() {
		return BUFFER_MAX_SIZE;
	}


}
