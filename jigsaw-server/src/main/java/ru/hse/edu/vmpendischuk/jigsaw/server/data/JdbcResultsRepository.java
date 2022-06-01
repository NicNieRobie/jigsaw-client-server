package ru.hse.edu.vmpendischuk.jigsaw.server.data;

import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStatsEntry;

import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The JDBC Derby implementation of the {@link ResultsRepository} interface.
 */
public class JdbcResultsRepository implements ResultsRepository {
    // Logger used to log messages.
    private static final Logger logger = Logger.getLogger(JdbcResultsRepository.class.getName());
    // Calendar instance used for timestamp parsing.
    private final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    // The database URL.
    private final String url;

    /**
     * Initializes a new {@link JdbcResultsRepository} instance.
     *
     * @param url the database URL.
     */
    public JdbcResultsRepository(String url) {
        this.url = url;
        createTable();
    }

    /**
     * Creates the table needed for the repository in the database.
     */
    private void createTable() {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE RESULTS(
                                        ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
                                        USERNAME VARCHAR(512) NOT NULL,
                                        FINISH_TIME TIMESTAMP NOT NULL,
                                        SCORE INTEGER NOT NULL,
                                        DURATION VARCHAR(255)
                )
                """);
        } catch (SQLException ex) {
            if (!ex.getSQLState().equals("X0Y32")) {
                logger.log(Level.SEVERE, "Could not create the results table!", ex);
                close();
            }
        }
    }

    /**
     * Adds the given record to the database.
     *
     * @param record the game result record.
     * @return the generated ID of the record in the database.
     * @throws SQLException if an error in the database communication has occurred.
     */
    @Override
    public long addRecord(PlayerStatsEntry record) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO RESULTS (USERNAME, FINISH_TIME, SCORE, DURATION) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, record.username());
            statement.setTimestamp(2, Timestamp.from(record.finishedAt()), cal);
            statement.setInt(3, record.score());
            statement.setString(4, record.time());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();
                return generatedKeys.getLong(1);
            }
        }
    }

    /**
     * Gets the top 10 (or less) game results from the database
     *   based on the corresponding sort.
     *
     * @return the top 10 (or less) game results.
     * @throws SQLException if an error in the database communication has occurred.
     */
    @Override
    public List<PlayerStatsEntry> getTopRecords() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             ResultSet records = statement.executeQuery("""
                    SELECT USERNAME, FINISH_TIME, SCORE, DURATION FROM RESULTS
                        ORDER BY SCORE DESC, DURATION, FINISH_TIME DESC FETCH NEXT 10 ROWS ONLY
                    """)) {
            List<PlayerStatsEntry> recordsList = new ArrayList<>();
            while (records.next()) {
                String username = records.getString("USERNAME");
                Instant finishTime = records.getTimestamp("FINISH_TIME", cal).toInstant();
                int score = records.getInt("SCORE");
                String duration = records.getString("DURATION");
                recordsList.add(new PlayerStatsEntry(username, score, duration, finishTime));
            }
            return recordsList;
        }
    }

    /**
     * Closes the database connection.
     */
    @Override
    public void close() {
        try {
            String shutdownUrl = "jdbc:derby:;shutdown=true";
            DriverManager.getConnection(shutdownUrl);
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("XJ015")) {
                logger.log(Level.INFO, "Derby DB shutdown successful");
            } else {
                logger.log(Level.WARNING, "Derby DB shutdown not successful", ex);
            }
        }
    }
}
