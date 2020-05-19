package mb.chatserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mb.sockethandling.ClientConnection;
import mb.sockethandling.ClientConnectionHandler;

public class ChatUser implements ClientConnectionHandler {

	public static final Logger log = Logger.getLogger(ChatUser.class.getSimpleName());

	private ChatServer chatServer;
	private ClientConnection clientConnection;
	private Thread clientConnectionThread;

	public ChatUser(ChatServer chatServer, ClientConnection clientConnection) {
		this.chatServer = chatServer;
		this.clientConnection = clientConnection;
		this.clientConnection.setConnectionHandler(this);
		startClientConnection();
		log.info("initialized new user");
	}

	private void startClientConnection() {
		this.clientConnectionThread = new Thread(clientConnection);
		this.clientConnectionThread.start();
		log.info("started client connection");
	}

	@Override
	public void onMessage(String message) {
		System.out.println("client message: " + message);
		chatServer.broadCast(message);
	}

	@Override
	public void onIOException(IOException exception) {
		clientConnection.terminate();
		chatServer.removeUser(this);
		log.log(Level.WARNING, "the user has encountered an unexpected exception", exception);
	}

	@Override
	public void onCompleted() {
		clientConnection.terminate();
		chatServer.removeUser(this);
		log.info("the user connection finished");
	}

	public ClientConnection getClientConnection() {
		return clientConnection;
	}

}
