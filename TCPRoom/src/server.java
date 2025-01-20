import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server implements Runnable {
// this class is going to look for connecitons and it is not going to have any method or any variable.

	private ArrayList<ConnectionHandler> connections;
	private ServerSocket server;
	private boolean done;
	private ExecutorService pool;

	public server() {
		connections = new ArrayList<>();
		done = false;
	}

	@Override

	public void run() {
		try {
			server = new ServerSocket(9999);
			pool = Executors.newCachedThreadPool();
			System.out.println("server started on port 9999");

			while (!done) {
				Socket client = server.accept();
				ConnectionHandler handler = new ConnectionHandler(client);
				connections.add(handler);
				pool.execute(handler);
			}
		} catch (Exception e) {
			// ignore

		}
	}

	public void broadcast(String message) {
		for (ConnectionHandler ch : connections) {
			if (ch != null) {
				ch.sendMessage(message);
			}

		}
	}

	public void shutdown() {
		try {
			done = true;
			if (!server.isClosed()) {
				server.close();
			} else {
				System.out.println("");

			}
			for (ConnectionHandler ch : connections) {
				ch.shutdown();
			}
		} catch (IOException e) {
			// ignore cause cannot be caught.
		}
	}

	class ConnectionHandler implements Runnable {
		private Socket client;
		private BufferedReader in;
		private PrintWriter out;
		private String nickname;

		public ConnectionHandler(Socket client) {
			this.client = client;
		}

		@Override

		public void run() {
			try {

				out = new PrintWriter(client.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out.println("Please enter a nickname: ");

				nickname = in.readLine().trim();
				System.out.println(nickname + " connected!");

				broadcast(nickname + " joined the chat: ");
				String message;

				while ((message = in.readLine()) != null) {
					if (message.startsWith("/nick")) {
						String[] messageSplit = message.split(" ", 2);

						if (messageSplit.length == 2) {
							broadcast(nickname + "Change their name to: " + messageSplit[1]);
							out.println(nickname + "nickname changed to ");

							nickname = messageSplit[1];
						} else {
							out.println("No nickname was entered: ");
						}
					} else if (message.startsWith("/quit")) {
						broadcast(nickname + " left the chat");
						shutdown();

					} else {
						broadcast(nickname + "" + message);
					}
				}

			} catch (IOException e) {
				shutdown();
			}
		}

		public void sendMessage(String message) {
			out.println(message);
		}

		public void shutdown() {
			try {
				in.close();
				out.close();
				if (!client.isClosed()) {
					client.close();
				}
			} catch (IOException e) {
				shutdown();

			}
		}
	}

	public static void main(String[] args) {
		server serve = new server();
		serve.run();
	}
}
