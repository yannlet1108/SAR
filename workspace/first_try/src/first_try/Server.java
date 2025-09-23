package first_try;

class Server implements Runnable {

	private int LENGTH_IN = 128;
	
	private int PORT = 12;

	@Override
	public void run() {
		Broker broker = Task.getBroker();
		Channel channel = broker.accept(PORT);
		
		byte[] text_in = new byte[LENGTH_IN];
		assert(!channel.disconnected()) : "Channel is disconnected";
		channel.read(text_in, 0, LENGTH_IN);
		System.out.println(text_in);
		
		byte[] text_out = ("Bonjour "+text_in.toString()).getBytes();
		assert(!channel.disconnected()) : "Channel is disconnected";
		channel.write(text_out, 0, text_out.length);
	}

}
