package ru.hse.edu.vmpendischuk.jigsaw.server;

import ru.hse.edu.vmpendischuk.jigsaw.server.game.GameStateManager;
import ru.hse.edu.vmpendischuk.jigsaw.util.GameResults;
import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStats;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JigsawServer {
    // The logger used to log messages.
    private static final Logger logger = Logger.getLogger(JigsawServer.class.getName());
    // The scanner used to read input.
    private static final Scanner scan = new Scanner(System.in);
    // The Jigsaw server game state manager.
    private static GameStateManager gameStateManager;

    /**
     * Jigsaw server entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        // Reading the port.
        int port;
        do {
            System.out.print("Enter the port (4000 <= n <= 9000): ");
            port = scan.nextInt();
        } while (port < 4000 || port > 9000);

        // Launching the server socket on given port.
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info(() -> "Started server, '" + serverSocket + "'");
            // Initializing the server.
            initializeGameServer();
            logger.info(() -> "Server initialized. Press any key to stop...");

            // Accepting clients.
            new Thread(() -> acceptClients(serverSocket)).start();
            System.in.read();

            // Shutting the database down on server shutdown.
            gameStateManager.closeDatabaseConnection();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Could not start the server!", ex);
        }
    }

    /**
     * Initializes the Jigsaw server game state.
     */
    private static void initializeGameServer() {
        int playerCount;
        int maxSec;
        String dbUrl;

        // Reading the player count.
        do {
            System.out.print("Select the number of players (1 / 2): ");
            playerCount = scan.nextInt();
        } while (playerCount != 1 && playerCount != 2);

        // Reading the max allowed game duration.
        do {
            System.out.print("Enter the max duration of a game in seconds (20 <= n <= 300): ");
            maxSec = scan.nextInt();
        } while (maxSec < 20 || maxSec > 300);

        // Reading the database URL.
        boolean urlIsValid = false;
        do {
            System.out.print("Enter the URL of a database (JDBC Derby Embedded only): ");
            dbUrl = scan.next();

            try {
                Connection connection = DriverManager.getConnection(dbUrl);
                urlIsValid = true;
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "DB connection error: " + ex.getSQLState());
            }
        } while (!urlIsValid);

        // Initializing the game state manager.
        JigsawServer.gameStateManager = new GameStateManager(playerCount, 20, maxSec, dbUrl);
    }

    /**
     * Accepts the client connections and serves them.
     *
     * @param serverSocket the server socket.
     */
    private static void acceptClients(ServerSocket serverSocket) {
        while (true) {
            try {
                // Serving client.
                Socket socket = serverSocket.accept();
                new Thread(() -> serveClient(socket)).start();
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                    break;
                }
                logger.log(Level.WARNING, e, () -> "Cannot serve client");
            }
        }
    }

    /**
     * Serves the client on given socket.
     *
     * @param socket the accepted client connection socket.
     */
    private static void serveClient(Socket socket) {
        String username = null;

        try (socket;
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                // Reading the request header.
                String header = inputStream.readUTF();
                switch (header) {
                    case "REGISTER" -> {
                        // If the request header is REGISTER - registering the user.

                        username = inputStream.readUTF();

                        if (!gameStateManager.connectPlayer(username)) {
                            outputStream.writeUTF("MAX PLAYER COUNT REACHED");
                            outputStream.flush();
                            logger.log(Level.INFO, "Player " + username + " not connected");
                            return;
                        }

                        while (!gameStateManager.allPlayersConnected()) {
                            outputStream.writeUTF("NOT READY");
                            outputStream.flush();
                        }

                        logger.log(Level.INFO, "Player " + username + " connected");
                        outputStream.writeUTF("READY");
                        outputStream.writeUTF(gameStateManager.getAnotherPlayer(username));
                        outputStream.writeInt(gameStateManager.getMaxDuration());
                        outputStream.flush();
                    }
                    case "STATUS" -> {
                        // If the request header is STATUS - returning the player connection status.

                        outputStream.writeBoolean(!gameStateManager.allPlayersConnected());
                        outputStream.flush();
                    }
                    case "GET SHAPE" -> {
                        // If the request header is GET SHAPE - returning a new shape for player.

                        outputStream.writeObject(gameStateManager.getShapeForPlayer(username));
                        outputStream.flush();
                    }
                    case "FINISH" -> {
                        // If the request header is FINISH - finishing the game for the player.

                        PlayerStats stats = (PlayerStats) inputStream.readObject();

                        if (!gameStateManager.finishForPlayer(username, stats)) {
                            outputStream.writeUTF("GAME ALREADY FINISHED");
                            outputStream.flush();
                            return;
                        }

                        while (!gameStateManager.allPlayersFinished()) {
                            outputStream.writeUTF("NOT READY");
                            outputStream.flush();
                        }

                        outputStream.writeUTF("READY");

                        GameResults results = gameStateManager.getResults();

                        outputStream.writeObject(results);
                        outputStream.flush();
                     }
                    case "DISCONNECT" -> {
                        // If the request header is DISCONNECT - disconnecting the player
                        //   and stopping serving the client.

                        if (username != null) {
                            gameStateManager.disconnectPlayer(username);
                            logger.log(Level.INFO, "Player " + username + " disconnected");
                        }

                        return;
                    }
                    case "RESTART" -> {
                        // If the request header is RESTART - restart the game for the player.

                        gameStateManager.reset();
                        gameStateManager.connectPlayer(username);

                        while (!gameStateManager.allPlayersConnected()) {
                            outputStream.writeUTF("NOT READY");
                            outputStream.flush();
                        }

                        outputStream.writeUTF(gameStateManager.getAnotherPlayer(username));
                        outputStream.flush();
                    }
                    case "TOP" -> {
                        // If the request header is TOP - return the top 10 (or less) game results.

                        outputStream.writeObject(gameStateManager.getTopRecords());
                        outputStream.flush();
                    }
                }
            }
        } catch (SocketException ex) {
            // If the client has closed the socket connection - disconnect the player.
            if (username != null) {
                gameStateManager.disconnectPlayer(username);
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.WARNING, "Could not process request", e);
        }
    }
}
