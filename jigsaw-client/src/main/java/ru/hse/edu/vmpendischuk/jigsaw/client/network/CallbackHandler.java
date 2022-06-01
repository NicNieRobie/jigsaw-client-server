package ru.hse.edu.vmpendischuk.jigsaw.client.network;

/**
 * Interface used as a wrapper for callback handling methods.
 */
public interface CallbackHandler {
    /**
     * Method called on operation success.
     */
    void onSuccess();

    /**
     * Method called on operation failure.
     *
     * @param errorMessage message which describes the error.
     */
    void onError(String errorMessage);
}
