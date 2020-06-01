package mb.sockethandling;

import mb.exceptions.ConnectionError;

public interface ClientConnectionHandler {

	void onMessage(String message);

	void onError(ConnectionError error);

	void onCompleted();

}
