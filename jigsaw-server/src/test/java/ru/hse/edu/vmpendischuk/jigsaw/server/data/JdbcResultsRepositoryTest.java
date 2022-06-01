package ru.hse.edu.vmpendischuk.jigsaw.server.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStatsEntry;

import java.sql.*;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that contains unit tests for the {@link JdbcResultsRepository} class methods.
 */
public class JdbcResultsRepositoryTest {
    // URL of a Derby Embedded database used for testing.
    private static final String URL = "jdbc:derby:testDb;create=true";
    // Logger used to log messages.
    private static final Logger logger = Logger.getLogger(JdbcResultsRepositoryTest.class.getName());
    // The result repository used for testing (the testing subject).
    private static final JdbcResultsRepository resultsRepository = new JdbcResultsRepository(URL);

    /**
     * Shuts the database down after all tests.
     */
    @AfterEach
    void closeConnection() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("XJ015")) {
                logger.log(Level.INFO, "Derby DB shutdown successful");
            }
        }
    }

    /**
     * Tests if the repository is properly initialized and creates a results table on initialization.
     */
    @Test
    @DisplayName("Initializes database connection validly")
    void initializes() {
        try (Connection connection = DriverManager.getConnection(URL)) {
            DatabaseMetaData dbmd = connection.getMetaData();
            ResultSet rs = dbmd.getTables(null, "APP", "RESULTS", null);
            Assertions.assertTrue(rs.next());
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Could not check the database for initialization");
        }
    }

    /**
     * Tests if the repository adds a new record to the database correctly.
     */
    @Test
    @DisplayName("Inserts new record correctly")
    void insertsRecord() {
        // Entry to be added.
        PlayerStatsEntry entry = new PlayerStatsEntry("Name", 1, "00:01:00", Instant.now());
        long id = 0;

        // Getting the ID of the entry in the database.
        try {
            id = resultsRepository.addRecord(entry);
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Could add record for test");
        }

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT USERNAME, FINISH_TIME, SCORE, DURATION FROM RESULTS WHERE ID = ?")) {
            // Fetching the entry with the same ID from the database.
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();

            // Check if an entry has been fetched.
            Assertions.assertTrue(rs.next());

            // Extracting the properties.
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            String username = rs.getString("USERNAME");
            Instant finishTime = rs.getTimestamp("FINISH_TIME", cal).toInstant();
            int score = rs.getInt("SCORE");
            String duration = rs.getString("DURATION");

            // Checking entries for equality.
            Assertions.assertAll(
                    () -> Assertions.assertEquals(entry.username(), username),
                    () -> Assertions.assertEquals(entry.finishedAt(), finishTime),
                    () -> Assertions.assertEquals(entry.score(), score),
                    () -> Assertions.assertEquals(entry.time(), duration)
            );
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Could not check the database for insertion");
        }
    }
}
