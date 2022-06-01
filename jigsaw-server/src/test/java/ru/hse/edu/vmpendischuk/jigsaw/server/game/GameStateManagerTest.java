package ru.hse.edu.vmpendischuk.jigsaw.server.game;

import org.junit.jupiter.api.*;
import ru.hse.edu.vmpendischuk.jigsaw.util.GameResults;
import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStats;

import java.time.Instant;

/**
 * Class that contains unit tests for the {@link GameStateManager} class methods.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameStateManagerTest {
    // The game state manager used for testing (the testing subject).
    private static final GameStateManager gameStateManager =
            new GameStateManager(2, 20, 300, "jdbc:derby:testDb;create=true");

    /**
     * Checks if the state manages initializes properly.
     */
    @Test
    @Order(1)
    @DisplayName("Initializes the game state manager properly")
    void initializes() {
        Assertions.assertAll(
                () -> Assertions.assertFalse(gameStateManager.allPlayersConnected()),
                () -> Assertions.assertFalse(gameStateManager.allPlayersFinished()),
                () -> Assertions.assertEquals(300, gameStateManager.getMaxDuration())
        );
    }

    /**
     * Checks if the state manager connects new players and manages their status properly.
     */
    @Test
    @Order(2)
    @DisplayName("Connects players and updates game status accordingly")
    void connectsPlayers() {
        Assertions.assertAll(
                () -> Assertions.assertTrue(gameStateManager.connectPlayer("Ivan")),
                () -> Assertions.assertTrue(gameStateManager.connectPlayer("Michael"))
        );

        Assertions.assertAll(
                () -> Assertions.assertFalse(gameStateManager.connectPlayer("Wilhelm")),
                () -> Assertions.assertTrue(gameStateManager.allPlayersConnected()),
                () -> Assertions.assertEquals("Ivan", gameStateManager.getAnotherPlayer("Michael")),
                () -> Assertions.assertEquals("Michael", gameStateManager.getAnotherPlayer("Ivan"))
        );
    }

    /**
     * Checks if the state manager manages the shape queue properly for all players.
     */
    @Test
    @Order(3)
    @DisplayName("Manages the shape queue and returns shapes for players")
    void managesShapes() {
        Assertions.assertNotNull(gameStateManager.getShapeForPlayer("Ivan"));
        Assertions.assertEquals(19, gameStateManager.getShapeCountForPlayer("Ivan"));

        for (int i = 0; i < 20; i++) {
            gameStateManager.getShapeForPlayer("Michael");
        }

        Assertions.assertAll(
                () -> Assertions.assertEquals(39, gameStateManager.getShapeCountForPlayer("Ivan")),
                () -> Assertions.assertEquals(20, gameStateManager.getShapeCountForPlayer("Michael"))
        );
    }

    /**
     * Checks if the state manager finishes the game properly for all players and correctly returns the winner.
     */
    @Test
    @Order(4)
    @DisplayName("Finished the game validly for players")
    void finishesGame() {
        PlayerStats ivanStats = new PlayerStats(10, "00:01:00", Instant.now());
        PlayerStats michaelStats = new PlayerStats(9, "00:00:20", Instant.now());

        Assertions.assertAll(
                () -> Assertions.assertTrue(gameStateManager.finishForPlayer("Ivan", ivanStats)),
                () -> Assertions.assertTrue(gameStateManager.finishForPlayer("Michael", michaelStats))
        );

        GameResults results = gameStateManager.getResults();

        Assertions.assertAll(
                () -> Assertions.assertTrue(gameStateManager.allPlayersFinished()),
                () -> Assertions.assertEquals(2, results.stats().size()),
                () -> Assertions.assertEquals("Ivan", results.winner()),
                () -> Assertions.assertTrue(results.disconnectedPlayers().isEmpty())
        );
    }
}
