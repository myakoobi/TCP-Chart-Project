import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private boolean done;

	@Override

	public void run() {
		try {
			Socket client = new Socket("127.0.0.1", 9999);
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			System.out.println("connected to the server");

			inputHandler inHandler = new inputHandler();
			Thread t = new Thread(inHandler);
			t.start();

			String inMessage;
			while ((inMessage = in.readLine()) != null) {

				System.out.println(inMessage);
			}

		} catch (IOException e) {
			shutdown();
		}
	}

	public void shutdown() {
		done = true;
		try {
			in.close();
			out.close();
			if (!client.isClosed()) {
				client.close();
			}
		} catch (IOException e) {
			// ignore this

		}

	}

	class inputHandler implements Runnable {

		@Override

		public void run() {
			try {
				BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
				while (!done) {
					String message = inReader.readLine();
					if (message.equals("/quit")) {
						inReader.close();
						shutdown();
					} else {
						out.println(message);
					}
				}

			} catch (IOException e) {
				shutdown();
			}
		}
	}

	public static void main(String[] args) {
		Client client = new Client();
		new Thread(client).start();
	}
}

