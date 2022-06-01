package ru.hse.edu.vmpendischuk.jigsaw.server.game;

import ru.hse.edu.vmpendischuk.jigsaw.server.data.JdbcResultsRepository;
import ru.hse.edu.vmpendischuk.jigsaw.server.data.ResultsRepository;
import ru.hse.edu.vmpendischuk.jigsaw.util.GameResults;
import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStats;
import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStatsEntry;
import ru.hse.edu.vmpendischuk.jigsaw.util.Shape;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Jigsaw game server state manager, used to track player stats and status,
 *   calculate and store Jigsaw game results.
 */
public class GameStateManager {
    // Logger used to log messages.
    private static final Logger logger = Logger.getLogger(GameStateManager.class.getName());
    // List of queues containing shapes generated for each player.
    private final List<Queue<Shape>> playerGeneratedShapes = new ArrayList<>();
    // Map of IDs corresponding to each player's username.
    private final Map<String, Integer> playerIds = new HashMap<>();
    // Map that contains each player's stats.
    private final Map<String, PlayerStats> playerStats = new HashMap<>();
    // List of disconnected players' usernames.
    private final List<String> disconnectedPlayers = new ArrayList<>();
    // Repository used to store game results.
    private final ResultsRepository resultsRepository;
    // The max amount of players.
    private final int playerCount;
    // The increment of shape amount in the queues.
    private final int shapeCountIncrement;
    // The max allowed duration of a game.
    private final int maxDuration;
    // The amount of players currently connected.
    private int currentPlayerCount = 0;
    // The amount of disconnected players.
    private int disconnectedPlayersCount = 0;
    // The amount of players that have finished their game.
    private int finishedPlayerCount = 0;

    /**
     * Initializes a new {@link GameStateManager} instance.
     *
     * @param playerCount the max amount of players.
     * @param shapeCount the increment of shape amount in the queues.
     * @param maxDuration the max allowed duration of a game.
     * @param dbUrl URL of the database used to store game results.
     */
    public GameStateManager(int playerCount, int shapeCount, int maxDuration, String dbUrl) {
        this.playerCount = playerCount;
        this.maxDuration = maxDuration;
        shapeCountIncrement = shapeCount;
        resultsRepository = new JdbcResultsRepository(dbUrl);

        for (int i = 0; i < playerCount; i++) {
            playerGeneratedShapes.add(new LinkedList<>());
        }

        for (int i = 0; i < shapeCount; i++) {
            Shape generatedShape = Shape.getRandomShape();
            for (int j = 0; j < playerCount; j++) {
                playerGeneratedShapes.get(j).offer(generatedShape);
            }
        }
    }

    /**
     * Connects the player with given username to the game.
     *
     * @param username player username.
     * @return flag that denotes if the player was allowed to connect to the game.
     */
    public boolean connectPlayer(String username) {
        if (currentPlayerCount == playerCount) {
            return false;
        }

        playerIds.put(username, currentPlayerCount++);
        return true;
    }

    /**
     * Disconnects the player with given username from the game.
     *
     * @param username player username.
     */
    public void disconnectPlayer(String username) {
        if (playerIds.containsKey(username)) {
            playerStats.remove(username);
            playerIds.remove(username);
            disconnectedPlayers.add(username);
            currentPlayerCount--;

            if (playerCount > 1) {
                disconnectedPlayersCount++;
            }

            if (currentPlayerCount == 0) {
                reset();
            }
        }
    }

    /**
     * Resets the game state to allow the game to restart.
     */
    public void reset() {
        disconnectedPlayersCount = 0;
        finishedPlayerCount = 0;

        playerGeneratedShapes.clear();
        playerStats.clear();
        disconnectedPlayers.clear();

        for (int i = 0; i < playerCount; i++) {
            playerGeneratedShapes.add(new LinkedList<>());
        }

        for (int i = 0; i < shapeCountIncrement; i++) {
            Shape generatedShape = Shape.getRandomShape();
            for (int j = 0; j < playerCount; j++) {
                playerGeneratedShapes.get(j).offer(generatedShape);
            }
        }
    }

    /**
     * Finished the game for the player with given username.
     *
     * @param username the player's username.
     * @param stats the player's stats.
     * @return flag that denotes if the game was successfully finished.
     */
    public boolean finishForPlayer(String username, PlayerStats stats) {
        if (finishedPlayerCount == playerCount - disconnectedPlayersCount) {
            return false;
        }

        playerStats.put(username, stats);
        finishedPlayerCount++;

        if (finishedPlayerCount == playerCount - disconnectedPlayersCount) {
            currentPlayerCount = 0;
            playerIds.clear();
        }

        return true;
    }

    /**
     * Checks if the necessary amount of players has connected to the game.
     *
     * @return flag that denotes if the necessary amount of players has connected to the game.
     */
    public boolean allPlayersConnected() {
        return currentPlayerCount == playerCount;
    }

    /**
     * Checks if the necessary amount of players has finished the game.
     *
     * @return flag that denotes if the necessary amount of players has finished the game.
     */
    public boolean allPlayersFinished() {
        return finishedPlayerCount == playerCount - disconnectedPlayersCount;
    }

    /**
     * Returns a new generated shape for the player to place.
     *
     * @param username the player's username.
     * @return the new shape.
     */
    public Shape getShapeForPlayer(String username) {
        Shape shape = playerGeneratedShapes.get(playerIds.get(username)).poll();

        if (getShapeCountForPlayer(username) == 0) {
            generateAdditionalShapes();
        }

        return shape;
    }

    /**
     * Returns the amount of shapes left in the user's generated shapes queue.
     *
     * @param username the player's username.
     * @return the amount of shapes left in the user's generated shapes queue.
     */
    public int getShapeCountForPlayer(String username) {
        return playerGeneratedShapes.get(playerIds.get(username)).size();
    }

    /**
     * Returns the results of the game.
     *
     * @return the results of the game.
     */
    public GameResults getResults() {
        List<Map.Entry<String, PlayerStats>> mapList = new ArrayList<>(playerStats.entrySet());
        mapList.sort(Map.Entry.comparingByValue());
        String winner = mapList.get(0).getKey();

        List<PlayerStatsEntry> entries = mapList.stream().map(entry -> {
            PlayerStats stats = entry.getValue();
            return new PlayerStatsEntry(stats, entry.getKey());
        }).toList();

        for (PlayerStatsEntry entry : entries) {
            try {
                resultsRepository.addRecord(entry);
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Could not save a record in the database", ex);
            }
        }

        return new GameResults(winner, entries, disconnectedPlayers.stream().toList());
    }

    /**
     * Returns the username of the player's opponent.
     *
     * @param username the player's username.
     * @return the username of the player's opponent.
     */
    public String getAnotherPlayer(String username) {
        String otherName = "";

        for (String name : playerIds.keySet()) {
            if (!name.equals(username)) {
                otherName = name;
            }
        }

        return otherName;
    }

    /**
     * Returns the top 10 (or less) Jigsaw game results.
     *
     * @return the top 10 (or less) Jigsaw game results.
     */
    public List<PlayerStatsEntry> getTopRecords() {
        try {
            return resultsRepository.getTopRecords();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not load top records", ex);
            return new ArrayList<>();
        }
    }

    /**
     * Returns the max allowed duration of the game.
     *
     * @return the max allowed duration of the game.
     */
    public int getMaxDuration() {
        return maxDuration;
    }

    /**
     * Closes the database connection for the repository.
     */
    public void closeDatabaseConnection() {
        resultsRepository.close();
    }

    /**
     * Generates additional shapes for all players.
     */
    private void generateAdditionalShapes() {
        for (int i = 0; i < playerCount; i++) {
            Shape generatedShape = Shape.getRandomShape();
            for (int j = 0; j < shapeCountIncrement; j++) {
                playerGeneratedShapes.get(i).offer(generatedShape);
            }
        }
    }
}
