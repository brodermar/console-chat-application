package mb.sockethandling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import mb.exceptions.ConnectionError;

public class SocketHandler implements ClientConnection {

	public static final Logger log = Logger.getLogger(SocketHandler.class.getSimpleName());

	private Object closeLock = new Object();
	private Object connectLock = new Object();

	private ClientConnectionHandler clientConnectionHandler;
	private boolean closed;
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;

	public static SocketHandler newSocketHandler(String ip, int port) {
		try {
			Socket socket = new Socket(ip, port);
			try {
				return new SocketHandler(socket);
			} catch (IOException e) {
				log.log(Level.WARNING, "failed to instantiate socket handler for socket " + socket);
				socket.close();
				throw new IllegalArgumentException(e);
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "couldn't create socket for " + ip + ":" + port, e);
			throw new IllegalArgumentException(e);
		}
	}

	public SocketHandler(Socket socket) throws IOException {
		this.closed = false;
		if (socket == null) {
			throw new IllegalArgumentException("socket is null");
		}
		this.socket = socket;
		try {
			this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
					BufferedWriter o = out;
					Socket s = socket;

			) {
				o.close();
				i.close();
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			closed = true;
			log.info("closed socket handler for connection to " + socket.getInetAddress() + ":" + socket.getPort());
		}
	}

	@Override
	public void run() {
		if (!isConnected()) {
			throw new IllegalStateException("the connection is not connected to a connection handler");
		}
		if (isClosed()) {
			throw new IllegalStateException("the connection was already closed");
		}
		log.info("started input stream handling for connection to " + socket.getInetAddress() + ":" + socket.getPort());
		try {
			while (!Thread.currentThread().isInterrupted() && !isClosed()) {
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
				try {
					close();
				} catch (IOException e1) {
					log.log(Level.FINE, "exception on close was thrown", e1);
				}
				clientConnectionHandler.onError(new ConnectionError(
						"exception on buffered reader was thrown, connection was closed as a result", e));
			} else {
				log.log(Level.CONFIG, "exception on input stream was thrown, but connection was already closed", e);
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
			clientConnectionHandler.onError(new ConnectionError("connection was already closed"));
		} else {
			try {
				synchronized (out) {
					out.write(message);
					out.newLine();
					out.flush();
				}
			} catch (IOException e) {
				try {
					close();
				} catch (IOException e1) {
					log.log(Level.FINE, "exception on close was thrown", e1);
				}
				clientConnectionHandler.onError(
						new ConnectionError("exception on buffered writer was thrown, connection was closed", e));
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
