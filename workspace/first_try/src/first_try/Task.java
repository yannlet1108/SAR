package first_try;

abstract class Task {
	Task(Broker b, Runnable r);
	static Broker getBroker();
}
