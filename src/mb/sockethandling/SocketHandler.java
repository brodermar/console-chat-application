package mb.sockethandling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketHandler implements ClientConnection {

	public static final Logger log = Logger.getLogger(SocketHandler.class.getSimpleName());

	private Object closeLock = new Object();
	private Object connectLock = new Object();

	private ClientConnectionHandler clientConnectionHandler;
	private boolean closed;
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;

	public static SocketHandler newSocketHandler(String ip, int port) {
		Socket socket = null;
		try {
			socket = new Socket(ip, port);
			return new SocketHandler(socket);
		} catch (IOException e) {
			log.log(Level.WARNING, "couldn't create socket for " + ip + ":" + port, e);
			throw new IllegalArgumentException(e);
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					log.log(Level.FINE, "exception throw during close", e);
				}
			}
		}
	}

	public SocketHandler(Socket socket) throws IOException {
		this.closed = false;
		if (socket == null) {
			throw new IllegalArgumentException("socket is null");
		}
		this.socket = socket;
		try {
			this.out = new PrintWriter(socket.getOutputStream());
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			try {
				close();
			} catch (IOException e1) {
				log.log(Level.FINE, "exception thrown during close", e);
			}
			throw e;
		}
		log.info("inialized new socket handler for connection to " + socket.getInetAddress() + ":" + socket.getPort());
	}

	@Override
	public boolean isTerminated() {
		return isClosed();
	}

	private boolean isClosed() {
		synchronized (closeLock) {
			return closed;
		}
	}

	private void close() throws IOException {
		synchronized (closeLock) {
			if (isClosed()) {
				return;
			}
			try (

					BufferedReader i = in;
					PrintWriter o = out;
					Socket s = socket;

			) {
				o.close();
				i.close();
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			log.info("closed socket handler for connection to " + socket.getInetAddress() + ":" + socket.getPort());
		}
	}

	@Override
	public void run() {
		log.info("started input stream handling for connection to " + socket.getInetAddress() + ":" + socket.getPort());
		if (!isConnected()) {
			throw new IllegalArgumentException("the connection is not connected to a connection handler");
		}
		try {
			while (!Thread.currentThread().isInterrupted()) {
				if (in.ready()) {
					String message = in.readLine();
					if (message != null) {
						clientConnectionHandler.onMessage(message);
					} else {
						clientConnectionHandler.onCompleted();
						close();
						Thread.currentThread().interrupt();
					}
				}
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		} catch (IOException e) {
			if (!isClosed()) {
				clientConnectionHandler.onIOException(e);
			} else {
				log.log(Level.CONFIG, "exception on input stream was thrown, but handler was already closed", e);
			}
		} finally {
			try {
				close();
			} catch (IOException e) {
				log.log(Level.FINE, "exception on close was thrown", e);
			}
		}
		log.info(
				"finished input stream handling for connection to " + socket.getInetAddress() + ":" + socket.getPort());
	}

	@Override
	public void sendMessage(String message) {
		if (!isConnected()) {
			throw new IllegalArgumentException("the connection is not connected to a connection handler");
		} else if (isClosed()) {
			clientConnectionHandler.onIOException(new IOException("the connection was already closed"));
		} else {
			out.println(message);
			if (out.checkError()) {
				try {
					close();
				} catch (IOException e) {
					log.log(Level.FINE, "exception on close was thrown", e);
				}
				clientConnectionHandler.onIOException(new IOException("error on outputstream detected"));
			}
		}
	}

	@Override
	public void terminate() {
		try {
			close();
		} catch (IOException e) {
			log.log(Level.FINE, "exception on close was thrown", e);
		}
	}

	public boolean isConnected() {
		synchronized (connectLock) {
			return clientConnectionHandler != null;
		}
	}

	@Override
	public void setConnectionHandler(ClientConnectionHandler clientConnectionHandler) {
		if (clientConnectionHandler == null) {
			throw new IllegalArgumentException("client connection handler is null");
		}
		synchronized (connectLock) {
			if (!isConnected()) {
				this.clientConnectionHandler = clientConnectionHandler;
				log.info("connected socket handler to connection handler");
			} else {
				throw new IllegalArgumentException("the connection is already connected to a connection handler");
			}
		}
	}

}
