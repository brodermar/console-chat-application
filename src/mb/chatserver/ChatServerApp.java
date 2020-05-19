package mb.chatserver;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServerApp {

	public static final Level LOG_LEVEL = Level.CONFIG;
	public static final int PORT = 9999;

	public static void main(String[] args) {
		Logger root = Logger.getLogger("");
		root.setLevel(LOG_LEVEL);
		for (Handler handler : root.getHandlers()) {
			handler.setLevel(LOG_LEVEL);
		}

		ChatServer chatServer = new ChatServer(PORT);
		chatServer.awaitTermination();
	}

}
