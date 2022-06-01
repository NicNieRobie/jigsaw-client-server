package ru.hse.edu.vmpendischuk.jigsaw.client.network;

import ru.hse.edu.vmpendischuk.jigsaw.util.Shape;

/**
 * Interface that describes behaviour of a client, used by the Jigsaw game app
 *   to communicate with the server.
 */
public interface JigsawClient {
    /**
     * Connects to the server with given address parameters.
     *
     * @param host server host.
     * @param port server port.
     * @param handler wrapper for the action callback handlers.
     */
    void connect(String host, int port, CallbackHandler handler);

    /**
     * Registers a player with given username in the system.
     *
     * @param username username.
     * @param handler wrapper for the action callback handlers.
     */
    void registerPlayer(String username, CallbackHandler handler);

    /**
     * Finishes the game for the registered player with given results.
     *
     * @param shapeCount the amount of shapes successfully placed by the player.
     * @param time the amount of time it took for the player to finish the game.
     * @param handler wrapper for the action callback handlers.
     */
    void finishGameForPlayer(int shapeCount, String time, GameFinishCallbackHandler handler);

    /**
     * Fetches the top 10 game results for the server.
     *
     * @param handler wrapper for the action callback handlers.
     */
    void getTopGames(TopGamesCallbackHandler handler);

    /**
     * Fetches a new shape for the player to place.
     *
     * @return a new shape for the player to place.
     */
    Shape getPlayerShape();

    /**
     * Checks if the client is connected to a server.
     *
     * @return flag that denotes if the client is connected to a server.
     */
    boolean isConnected();

    /**
     * Disconnects the client from the server.
     */
    void disconnect();

    /**
     * Restarts the game for the registered player.
     *
     * @param handler wrapper for the action callback handlers.
     */
    void restart(CallbackHandler handler);

    /**
     * Returns the player's username.
     *
     * @return the player's username.
     */
    String getUsername();

    /**
     * Returns the opponent's username.
     *
     * @return the opponent's username.
     */
    String getOtherUsername();

    /**
     * Returns the game's maximum duration in seconds.
     *
     * @return the game's maximum duration in seconds.
     */
    int getMaxDuration();
}
