package mb.sockethandling;

import java.io.IOException;

public interface ClientConnectionHandler {

	void onMessage(String message);

	void onIOException(IOException exception);

	void onCompleted();

}
