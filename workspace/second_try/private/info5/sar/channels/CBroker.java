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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CBroker extends Broker {

	// For each port with at least a pending connection, the list of current RDVs
	// If there is one pending accept, it is placed first in the list
	private final Map<Integer, ArrayList<RDV>> rdvs;

	public CBroker(String name) {
		super(name);
		BrokerManager.getInstance().addBroker(this);
		rdvs = new HashMap<>();
	}

	@Override
	public Channel accept(int port) {
		RDV rdv;
		synchronized (rdvs) {

			if (rdvs.containsKey(port)) {
				RDV firstRDV = rdvs.get(port).get(0);
				if (firstRDV.getPendingOperation() == PendingOperation.ACCEPT) {
					throw new IllegalArgumentException(
							"A Task is already accepting on the port " + port + " of the broker " + getName());
				} else {
					rdv = firstRDV;
					rdvs.get(port).remove(firstRDV); // match found so RDV removed
					// To clean the map
					if (rdvs.get(port).size() == 0) {
						rdvs.remove(port);
					}
				}
			}

			else { // no task is waiting on this port
				rdv = new RDV(2, PendingOperation.ACCEPT); // 2 threads expected, the 2 endpoints
				ArrayList<RDV> newList = new ArrayList<RDV>();
				newList.add(rdv);
				rdvs.put(port, newList);
			}
		}
		return rdv.come(this, port);
	}

	@Override
	public Channel connect(String name, int port) {
		Broker targetBroker;
		try {
			targetBroker = BrokerManager.getInstance().getBroker(name);
		} catch (IllegalArgumentException e) {
			return null;
		}
		CBroker targetCBroker = (CBroker) targetBroker;
		return targetCBroker.addConnect(port);
	}

	private Channel addConnect(int port) {
		RDV rdv;
		synchronized (rdvs) {
			// if some task are already waiting on this port (accept or connect)
			if (rdvs.containsKey(port)) {
				// check if there is one pending accept (first position)
				RDV firstRDV = rdvs.get(port).get(0);
				if (firstRDV.getPendingOperation() == PendingOperation.CONNECT) {
					rdv = new RDV(2, PendingOperation.CONNECT);
					rdvs.get(port).add(rdv); // added at the end of the connect queue
				} else {
					rdv = firstRDV;
					rdvs.get(port).remove(firstRDV); // match found so RDV removed
					// To clean the map
					if (rdvs.get(port).size() == 0) {
						rdvs.remove(port);
					}
				}

			} else { // no task is waiting on this port
				rdv = new RDV(2, PendingOperation.CONNECT); // 2 threads expected, the 2 endpoints
				ArrayList<RDV> newList = new ArrayList<RDV>();
				newList.add(rdv);
				rdvs.put(port, newList);
			}
		}
		return rdv.come(this, port);
	}
	
	public boolean removeAccept(int port) {
		
		RDV firstRDV = rdvs.get(port).get(0);
		if (firstRDV.getPendingOperation() == PendingOperation.CONNECT) {
			return false; // no pending accept to be removed
		}
		
		rdvs.get(port).remove(firstRDV);
		return true;
	}

}
