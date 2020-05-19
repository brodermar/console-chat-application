package mb.chatclient;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatClientApp {

	public static final Level LOG_LEVEL = Level.FINE;
	public static final String IP = "127.0.0.1";
	public static final int PORT = 9999;

	public static void main(String[] args) {
		Logger root = Logger.getLogger("");
		root.setLevel(LOG_LEVEL);
		for (Handler handler : root.getHandlers()) {
			handler.setLevel(LOG_LEVEL);
		}

		ChatClient chatClient = new ChatClient(IP, PORT);
		chatClient.run();
	}

}
