package mb.chatclient;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import mb.sockethandling.ClientConnection;

public class ChatClientInputHandler implements Runnable {

	public static final Logger log = Logger.getLogger(ChatClientInputHandler.class.getName());

	private ClientConnection clientConnection;

	public ChatClientInputHandler(ClientConnection clientConnection) {
		this.clientConnection = clientConnection;
	}

	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);
		String line = null;
		while (!Thread.currentThread().isInterrupted() && !clientConnection.isTerminated()) {
			line = scanner.nextLine();
			clientConnection.sendMessage(line);
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				log.log(Level.FINE, "thread was interrupted during sleep", e);
				Thread.currentThread().interrupt();
			}
		}
		scanner.close();
	}

}
