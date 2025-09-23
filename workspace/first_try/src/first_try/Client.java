package first_try;

class Client implements Runnable {
	
	private int LENGTH_IN = 128;
	
	private int SERVER_PORT = 12;

	@Override
	public void run() {
		Broker broker = Task.getBroker();
		Channel channel = broker.connect("server", SERVER_PORT);
		
		byte[] text_out = "Client1".getBytes();
		assert(!channel.disconnected()) : "Channel is disconnected";
		channel.write(text_out, 0, text_out.length);
		
		byte[] text_in = new byte[LENGTH_IN];
		assert(!channel.disconnected()) : "Channel is disconnected";
		channel.read(text_in, 0, LENGTH_IN);
		System.out.println(text_in);
		
		channel.disconnect();
	}

}
