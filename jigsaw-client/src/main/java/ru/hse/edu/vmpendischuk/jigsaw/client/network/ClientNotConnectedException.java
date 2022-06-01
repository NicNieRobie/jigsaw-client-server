package ru.hse.edu.vmpendischuk.jigsaw.client.network;

/**
 * Exception thrown when the {@link JigsawClient} used by the app
 *   was not connected to the server at the time of a method call.
 */
public class ClientNotConnectedException extends RuntimeException {
    public ClientNotConnectedException(String message) {
        super(message);
    }
}
