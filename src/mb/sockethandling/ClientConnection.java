package mb.sockethandling;

public interface ClientConnection extends Runnable {

	void setConnectionHandler(ClientConnectionHandler connectionHandler);

	void sendMessage(String message);

	void terminate();

	boolean isTerminated();

	boolean isConnected();

	public static ClientConnection newClientConnection(String ip, int port) {
		return SocketHandler.newSocketHandler(ip, port);
	}

}
