package mb.sockethandling;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerSocketHandler implements Runnable {

	public static final Logger log = Logger.getLogger(ServerSocketHandler.class.getName());

	private int port;
	private ServerSocketObserver obs;

	public ServerSocketHandler(int port, ServerSocketObserver obs) {
		this.port = port;
		if (obs == null) {
			throw new IllegalArgumentException("observer is null");
		}
		this.obs = obs;
		log.info("initalized new server socket handler for port " + port);
	}

	@Override
	public void run() {
		log.info("startet server socket handler");
		try (ServerSocket serverSocket = new ServerSocket(port)) {

			log.info("bound server socket to " + serverSocket.getLocalSocketAddress());
			serverSocket.setSoTimeout(200);

			while (!Thread.currentThread().isInterrupted()) {

				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					log.log(Level.CONFIG, "thread sleep was interrupted, thread will be terminated", e);
					Thread.currentThread().interrupt();
				}

				try {
					Socket socket = serverSocket.accept();
					try {
						obs.onNewConnection(new SocketHandler(socket));
					} catch (IOException e) {
						log.log(Level.SEVERE, "failed to create socket handler for socket " + socket, e);
						Thread.currentThread().interrupt();
					} finally {
						socket.close();
					}
				} catch (SocketTimeoutException e) {
					log.log(Level.FINE, "expected timeout exception was thrown", e);
				} catch (IOException e) {
					log.log(Level.SEVERE, "unexpected excpetion was thrown while waiting for new connection", e);
					Thread.currentThread().interrupt();
				}

			}

		} catch (SocketException e) {
			log.log(Level.SEVERE, "error occured in the underlying protocol, such as a TCP error", e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "failed to open a server socket for port " + port, e);
		}
		log.info("terminated server socket handler");
	}

}
