package mb.socketexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {

	public static final int PORT = 6666;
	public static final String CLOSING = "good bye";
	public static final String TERMINATE = ".";

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
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				System.out.println("echo client message: " + inputLine);
				if (TERMINATE.equals(inputLine)) {
					out.println(CLOSING);
					break;
				}
				out.println(inputLine);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("server failed", e);
		}

	}

	public static void main(String[] args) {
		EchoServer server = new EchoServer();
		server.start(PORT);
	}

}
