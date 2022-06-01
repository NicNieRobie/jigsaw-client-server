package ru.hse.edu.vmpendischuk.jigsaw.client.network;

import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStatsEntry;

import java.util.List;

/**
 * Interface used as a wrapper for top 10 games request callback handling methods.
 */
public interface TopGamesCallbackHandler {
    /**
     * Method called on operation success.
     *
     * @param topGames top 10 game results.
     */
    void onSuccess(List<PlayerStatsEntry> topGames);

    /**
     * Method called on operation failure.
     *
     * @param errorMessage message which describes the error.
     */
    void onError(String errorMessage);
}
