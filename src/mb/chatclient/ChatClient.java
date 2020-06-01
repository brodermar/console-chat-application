package mb.chatclient;

import java.util.logging.Level;
import java.util.logging.Logger;

import mb.exceptions.ConnectionError;
import mb.sockethandling.ClientConnection;
import mb.sockethandling.ClientConnectionHandler;

public class ChatClient implements ClientConnectionHandler {

	public static final Logger log = Logger.getLogger(ChatClient.class.getName());

	private ClientConnection clientConnection;
	private ChatClientInputHandler inputHandler;

	public ChatClient(String ip, int port) {
		this.clientConnection = ClientConnection.newClientConnection(ip, port);
		this.clientConnection.setConnectionHandler(this);
		this.inputHandler = new ChatClientInputHandler(this.clientConnection);
	}

	public void run() {
		Thread clientConnectionThread = new Thread(clientConnection);
		clientConnectionThread.start();
		Thread inputHandlerThread = new Thread(inputHandler);
		inputHandlerThread.start();
		try {
			clientConnectionThread.join();
			inputHandlerThread.join();
		} catch (InterruptedException e) {
			log.log(Level.SEVERE, "thread was interrupted", e);
		}
	}

	@Override
	public void onMessage(String message) {
		System.out.println("message from server: " + message);
	}

	@Override
	public void onError(ConnectionError exception) {
		log.log(Level.SEVERE, "received unexpected exception from connection handler", exception);
	}

	@Override
	public void onCompleted() {
		log.log(Level.INFO, "received completed message from client connection");
	}

}
