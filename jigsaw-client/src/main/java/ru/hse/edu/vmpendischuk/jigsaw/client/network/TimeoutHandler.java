package ru.hse.edu.vmpendischuk.jigsaw.client.network;

/**
 * Interface used as a wrapper for timeout handling methods.
 */
public interface TimeoutHandler {
    /**
     * Method called on timeout.
     */
    void onTimeout();
}
