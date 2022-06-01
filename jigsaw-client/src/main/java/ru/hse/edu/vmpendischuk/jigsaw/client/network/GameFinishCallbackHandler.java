package ru.hse.edu.vmpendischuk.jigsaw.client.network;

import ru.hse.edu.vmpendischuk.jigsaw.util.GameResults;

/**
 * Interface used as a wrapper for game finish request callback handling methods.
 */
public interface GameFinishCallbackHandler {
    /**
     * Method called on operation success.
     *
     * @param gameResults the game results.
     */
    void onSuccess(GameResults gameResults);

    /**
     * Method called on operation failure.
     *
     * @param errorMessage message which describes the error.
     */
    void onError(String errorMessage);
}
