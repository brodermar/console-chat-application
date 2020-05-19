package mb.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mb.chatserver.ChatServerApp;
import mb.socketexample.EchoClient;
import mb.socketexample.EchoServer;
import mb.socketexample.GreetClient;
import mb.socketexample.GreetServer;

class ClientTest {

	public static final Logger log = Logger.getLogger(ClientTest.class.getName());

	public static final Level LOG_LEVEL = Level.CONFIG;

	@BeforeAll
	static void setUpAll() {
		Logger root = Logger.getLogger("");
		root.setLevel(LOG_LEVEL);
		for (Handler handler : root.getHandlers()) {
			handler.setLevel(LOG_LEVEL);
		}
	}

	@Test
	void greetTest() {
		Thread server = new Thread(() -> GreetServer.main(new String[] {}));
		server.start();
		GreetClient client = new GreetClient();
		String response = client.sendMessage("127.0.0.1", GreetServer.PORT, "hello server");
		System.out.println("greet server message: " + response);
		try {
			server.join();
		} catch (InterruptedException e) {
			fail(e);
		}
		assertEquals(GreetServer.SERVER_MESSAGE, response);
		assertEquals(State.TERMINATED, server.getState());
	}

	@Test
	void echoTest() {
		Thread server = new Thread(() -> EchoServer.main(new String[] {}));
		server.start();
		String[] messages = { "What", "the", "fuck", "are", "you", "doing?" };
		try (EchoClient client = new EchoClient("127.0.0.1", EchoServer.PORT)) {
			for (String message : messages) {
				String response = client.sendMessage(message);
				System.out.println("echo server message: " + response);
				assertEquals(message, response);
			}
			String response = client.sendMessage(EchoServer.TERMINATE);
			System.out.println("echo server message: " + response);
			assertEquals(EchoServer.CLOSING, response);
		} catch (IOException e) {
			fail(e);
		}
		try {
			server.join();
		} catch (InterruptedException e) {
			fail(e);
		}
		assertEquals(State.TERMINATED, server.getState());
	}

	@Test
	void chatServerGreetClientTest() {
		Thread server = new Thread(() -> ChatServerApp.main(new String[] {}));
		server.start();
		GreetClient greetClient = new GreetClient();
		String response = greetClient.sendMessage("127.0.0.1", ChatServerApp.PORT, "test");
		System.out.println("response: " + response);
		server.interrupt();
		try {
			server.join();
		} catch (InterruptedException e) {
			fail(e);
		}
	}

	@Test
	void chatServerEchoClientTest() {
		Thread server = new Thread(() -> ChatServerApp.main(new String[] {}));
		server.start();
		String[] messages = { "What", "the", "fuck", "are", "you", "doing?" };
		try (EchoClient client = new EchoClient("127.0.0.1", ChatServerApp.PORT)) {
			for (String message : messages) {
				String response = client.sendMessage(message);
				System.out.println("echo server message: " + response);
			}
		} catch (IOException e) {
			fail(e);
		} finally {
			if (!server.isInterrupted()) {
				server.interrupt();
			}
			try {
				server.join();
			} catch (InterruptedException e) {
				fail(e);
			}
		}
		if (!server.isInterrupted()) {
			server.interrupt();
		}
		try {
			server.join();
		} catch (InterruptedException e) {
			fail(e);
		}
	}

}
