package info5.sar.channels;

import java.util.HashMap;
import java.util.Map;

public class BrokerManager {

	private static BrokerManager instance;

	private static final Map<String, Broker> brokers = new HashMap<>();

	public static BrokerManager getInstance() {
		if (instance == null) {
			instance = new BrokerManager();
		}
		return instance;
	}

	public synchronized void addBroker(Broker broker) {
		if (brokers.containsKey(broker.getName())) {
			throw new IllegalArgumentException("A broker named " + broker.getName() + " already exists");
		}
		brokers.put(broker.getName(), broker);
	}

	public synchronized void removeBroker(String name) {
		brokers.remove(name);
	}

	public synchronized Broker getBroker(String name) {
		if (brokers.containsKey(name)) {
			return brokers.get(name);
		}
		throw new IllegalArgumentException("No broker found with the name : " + name);
	}
	
	synchronized boolean isBrokerExisting(String name) {
		return brokers.containsKey(name);
	}
}

