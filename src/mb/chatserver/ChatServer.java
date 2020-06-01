package mb.chatserver;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import mb.sockethandling.ClientConnection;
import mb.sockethandling.ServerSocketHandler;
import mb.sockethandling.ServerSocketObserver;

public class ChatServer implements ServerSocketObserver {

	public static final Logger log = Logger.getLogger(ChatServer.class.getName());

	private Object terminationLock = new Object();

	private Thread serverSocketHandlerThread;
	private Set<ChatUser> users;
	private boolean terminated;

	public ChatServer(int port) {
		this.serverSocketHandlerThread = new Thread(new ServerSocketHandler(port, this));
		this.users = ConcurrentHashMap.newKeySet();
		this.terminated = false;
		this.serverSocketHandlerThread.start();
		log.info("initialized chat server for port " + port);
	}

	public void awaitTermination() {
		while (!Thread.currentThread().isInterrupted() && serverSocketHandlerThread.isAlive()) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				log.log(Level.CONFIG, "catched exception and terminate the service now", e);
				terminate();
				Thread.currentThread().interrupt();
			}
		}
	}

	private void terminate() {
		synchronized (terminationLock) {
			if (isTerminated()) {
				return;
			}
			serverSocketHandlerThread.interrupt();
			Iterator<ChatUser> it = users.iterator();
			while (it.hasNext()) {
				it.next().getClientConnection().terminate();
				it.remove();
			}
		}
		log.info("terminated chatserver");
	}

	private boolean isTerminated() {
		synchronized (terminationLock) {
			return terminated;
		}
	}

	void removeUser(ChatUser chatUser) {
		users.remove(chatUser);
		log.log(Level.CONFIG, "removed user " + chatUser);
	}

	void addUser(ChatUser chatUser) {
		users.add(chatUser);
		log.log(Level.CONFIG, "added user " + chatUser);
	}

	public void broadCast(String message) {
		Iterator<ChatUser> userIt = users.iterator();
		while (userIt.hasNext()) {
			ChatUser user = userIt.next();
			if (!user.getClientConnection().isTerminated()) {
				user.getClientConnection().sendMessage(message);
			} else {
				userIt.remove();
			}
		}
		log.log(Level.CONFIG, "broadcasted message: \"" + message + "\"");
	}

	@Override
	public void onNewConnection(ClientConnection clientConnection) {
		synchronized (terminationLock) {
			if (!isTerminated()) {
				users.add(new ChatUser(this, clientConnection));
			}
		}
	}

}
