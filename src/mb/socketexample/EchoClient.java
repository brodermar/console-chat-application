package mb.socketexample;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class EchoClient implements Closeable {

	// socket to connect to the server
	private Socket clientSocket;
	// input and output streams
	private PrintWriter out;
	private BufferedReader in;

	public EchoClient(String ip, int port) throws UnknownHostException, IOException {
		// creates a new socket to connect to the server
		clientSocket = new Socket(ip, port);
		// opens input and output streams
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	public String sendMessage(String message) throws IOException {
		out.println(message);
		while (!in.ready()) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		String response = in.readLine();
		return response;
	}

	@Override
	public void close() throws IOException {
		try (BufferedReader i = in; PrintWriter o = out; Socket s = clientSocket) {
			i.close();
			o.close();
			s.close();
		}
	}

}
