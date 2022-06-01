package ru.hse.edu.vmpendischuk.jigsaw.client.network;

import ru.hse.edu.vmpendischuk.jigsaw.util.GameResults;
import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStats;
import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStatsEntry;
import ru.hse.edu.vmpendischuk.jigsaw.util.Shape;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The default implementation of the {@link JigsawClient} interface,
 *   used by the Jigsaw game app to communicate with the server.
 */
public class DefaultJigsawClient implements JigsawClient {
    // Inner class used to hold the singleton instance.
    // Used to implement the so-called "lazy initialization".
    private static class DefaultJigsawClientHolder {
        private static final DefaultJigsawClient INSTANCE = new DefaultJigsawClient();
    }

    // The logger instance used to log messages.
    private static final Logger logger = Logger.getLogger(DefaultJigsawClient.class.getName());
    // The client socket.
    private Socket socket;
    // The player's username.
    private String username = null;
    // The opponent's username.
    private String otherUsername = null;
    // Game's maximum allowed duration.
    private int maxDuration;
    // Socket input stream.
    private ObjectInputStream inputStream;
    // Socket output stream.
    private ObjectOutputStream outputStream;
    // Flag that denotes if the client is connected to the server.
    private boolean isConnected = false;

    private DefaultJigsawClient() { }

    /**
     * Returns the singleton instance of the {@link DefaultJigsawClient} class.
     *
     * @return the singleton instance of the {@link DefaultJigsawClient} class.
     */
    public static DefaultJigsawClient getInstance() {
        return DefaultJigsawClientHolder.INSTANCE;
    }

    /**
     * Connects to the server with given address parameters.
     *
     * @param host server host.
     * @param port server port.
     * @param handler wrapper for the action callback handlers.
     */
    @Override
    public void connect(String host, int port, CallbackHandler handler) {
        try {
            socket = new Socket(host, port);
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            isConnected = true;

            handler.onSuccess();
        } catch (IOException ex) {
            handler.onError("Could not connect to the server");
        }
    }

    /**
     * Registers a player with given username in the system.
     *
     * @param username username.
     * @param handler wrapper for the action callback handlers.
     */
    @Override
    public void registerPlayer(String username, CallbackHandler handler) {
        if (!isConnected) {
            throw new ClientNotConnectedException("The client is not connected to the server!");
        }

        try {
            outputStream.writeUTF("REGISTER");
            outputStream.writeUTF(username);
            outputStream.flush();

            String message = inputStream.readUTF();

            if (message.equals("MAX PLAYER COUNT REACHED")) {
                handler.onError("Max player count reached");
            } else {
                this.username = username;

                while (message.equals("NOT READY")) {
                    message = inputStream.readUTF();
                }

                otherUsername = inputStream.readUTF();
                maxDuration = inputStream.readInt();

                handler.onSuccess();
            }
        } catch (Exception ex) {
            handler.onError("Could not get server status");
        }
    }

    /**
     * Fetches a new shape for the player to place.
     *
     * @return a new shape for the player to place.
     */
    @Override
    public Shape getPlayerShape() {
        if (!isConnected) {
            throw new ClientNotConnectedException("The client is not connected to the server!");
        }

        try {
            outputStream.writeUTF("GET SHAPE");
            outputStream.flush();

            Shape shape = (Shape) inputStream.readObject();

            if (shape == null) {
                logger.log(Level.WARNING, "Could not load new shape");
            }

            return shape;
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Could not load new shape - connection lost");
            return null;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not load new shape", ex);
            return null;
        }
    }

    /**
     * Finishes the game for the registered player with given results.
     *
     * @param shapeCount the amount of shapes successfully placed by the player.
     * @param time the amount of time it took for the player to finish the game.
     * @param handler wrapper for the action callback handlers.
     */
    @Override
    public void finishGameForPlayer(int shapeCount, String time, GameFinishCallbackHandler handler) {
        if (!isConnected) {
            throw new ClientNotConnectedException("The client is not connected to the server!");
        }

        try {
            PlayerStats stats = new PlayerStats(shapeCount, time, Instant.now());
            outputStream.writeUTF("FINISH");
            outputStream.flush();
            outputStream.writeObject(stats);
            outputStream.flush();

            String message = inputStream.readUTF();

            if (message.equals("GAME ALREADY FINISHED")) {
                handler.onError("Game is already finished for user");
            } else {
                while (!message.equals("READY")) {
                    message = inputStream.readUTF();
                }

                GameResults results = (GameResults) inputStream.readObject();

                handler.onSuccess(results);
            }
        } catch (Exception ex) {
            handler.onError("Could not get server status");
        }
    }

    /**
     * Fetches the top 10 game results for the server.
     *
     * @param handler wrapper for the action callback handlers.
     */
    @Override
    public void getTopGames(TopGamesCallbackHandler handler) {
        if (!isConnected) {
            throw new ClientNotConnectedException("The client is not connected to the server!");
        }

        try {
            outputStream.writeUTF("TOP");
            outputStream.flush();

            List<PlayerStatsEntry> topGames = (List<PlayerStatsEntry>) inputStream.readObject();

            handler.onSuccess(topGames);
        } catch (Exception ex) {
            handler.onError("Could not get server response");
        }
    }

    /**
     * Checks if the client is connected to a server.
     *
     * @return flag that denotes if the client is connected to a server.
     */
    @Override
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Disconnects the client from the server.
     */
    @Override
    public void disconnect() {
        try {
            outputStream.writeUTF("DISCONNECT");
            System.out.println("DISCONNECT");
            outputStream.flush();

            socket.close();
            inputStream.close();
            outputStream.close();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Could not disconnect", ex);
        }
    }

    /**
     * Restarts the game for the registered player.
     *
     * @param handler wrapper for the action callback handlers.
     */
    @Override
    public void restart(CallbackHandler handler) {
        if (!isConnected) {
            throw new ClientNotConnectedException("The client is not connected to the server!");
        }

        try {
            outputStream.writeUTF("RESTART");
            outputStream.flush();

            String message = inputStream.readUTF();

            while (!message.equals("READY")) {
                message = inputStream.readUTF();
            }

            handler.onSuccess();
        } catch (Exception ex) {
            handler.onError("Could not get server status");
        }
    }

    /**
     * Returns the player's username.
     *
     * @return the player's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the opponent's username.
     *
     * @return the opponent's username.
     */
    public String getOtherUsername() {
        return otherUsername;
    }

    /**
     * Returns the game's maximum duration in seconds.
     *
     * @return the game's maximum duration in seconds.
     */
    public int getMaxDuration() {
        return maxDuration;
    }
}
