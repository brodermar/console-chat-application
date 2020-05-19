package mb.socketexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class GreetServer {

	public static final String SERVER_MESSAGE = "hello client";
	public static final int PORT = 6666;

	/**
	 * Starts the server listening for the given port to await single message of a
	 * single client.
	 * 
	 * @param port the port the server should listen to
	 */
	public void start(int port) {

		try (
				// opens new server socket for localhost and the given port
				ServerSocket serverSocket = new ServerSocket(port);

				// waits for a client
				Socket clientSocket = serverSocket.accept();

				// opens input and output stream
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))

		) {

			// reads the message and prints it to the console
			String clientMessage = in.readLine();
			System.out.println("greet client message: " + clientMessage);
			
			out.println(SERVER_MESSAGE);

		} catch (IOException e) {
			throw new IllegalArgumentException("failed to create socket for localhost and port " + port);
		}

	}

	public static void main(String[] args) {
		GreetServer server = new GreetServer();
		server.start(PORT);
	}

}
