package ru.hse.edu.vmpendischuk.jigsaw.util;

import java.io.Serializable;
import java.util.List;

/**
 * Record used to store the results of a Jigsaw game -
 *   the winner's username, player's stats and the list of disconnected players.
 */
public record GameResults(String winner, List<PlayerStatsEntry> stats, List<String> disconnectedPlayers) implements Serializable { }