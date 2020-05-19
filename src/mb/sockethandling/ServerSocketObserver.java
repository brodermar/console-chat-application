package mb.sockethandling;

public interface ServerSocketObserver {

	void onNewConnection(ClientConnection clientConnection);

}
