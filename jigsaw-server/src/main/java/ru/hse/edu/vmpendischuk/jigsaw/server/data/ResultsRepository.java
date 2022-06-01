package ru.hse.edu.vmpendischuk.jigsaw.server.data;

import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStatsEntry;

import java.sql.SQLException;
import java.util.List;

/**
 * The interface that describes behaviour of a game results repository.
 */
public interface ResultsRepository {
    /**
     * Adds the given record to the database.
     *
     * @param record the game result record.
     * @return the generated ID of the record in the database.
     * @throws SQLException if an error in the database communication has occurred.
     */
    long addRecord(PlayerStatsEntry record) throws SQLException;

    /**
     * Gets the top 10 (or less) game results from the database
     *   based on the corresponding sort.
     *
     * @return the top 10 (or less) game results.
     * @throws SQLException if an error in the database communication has occurred.
     */
    List<PlayerStatsEntry> getTopRecords() throws SQLException;

    /**
     * Closes the database connection.
     */
    void close();
}
