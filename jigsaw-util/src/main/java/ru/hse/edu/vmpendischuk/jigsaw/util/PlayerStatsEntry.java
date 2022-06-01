package ru.hse.edu.vmpendischuk.jigsaw.util;

import java.io.Serializable;
import java.time.Instant;

/**
 * Record used to tie the {@link PlayerStats} Jigsaw player stats to the player's username.
 */
public record PlayerStatsEntry(String username, Integer score, String time, Instant finishedAt) implements Serializable {
    public PlayerStatsEntry(PlayerStats stats, String username) {
        this(username, stats.score(), stats.time(), stats.finishedAt());
    }
}