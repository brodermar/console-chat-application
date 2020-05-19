package mb.socketexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class GreetClient {

	public static final String CLIENT_MESSAGE = "hello client";

	/**
	 * Sends a message to the server with the given ip and port.
	 * 
	 * @param ip      the ip of the server
	 * @param port    the port of the server
	 * @param message the message to send
	 * @return the response of the server
	 */
	public String sendMessage(String ip, int port, String message) {
		try (
				// creates a new socket to connect to the server
				Socket clientSocket = new Socket(ip, port);

				// opens input and output streams
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))

		) {

			// sends the message to the server
			out.println(message);
			// waits for response of the server
			String response = in.readLine();

			// closes the input and output streams
			in.close();
			out.close();

			// returns the server response
			return response;

		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("couldn't determine the host for the given ip adress " + ip);
		} catch (IOException e) {
			throw new IllegalArgumentException("failed to create socket for ip " + ip + " and port " + port);
		}

	}

}
