package ru.hse.edu.vmpendischuk.jigsaw.util;

import java.io.Serializable;
import java.time.Instant;

/**
 * Record used to store the Jigsaw player's stats - score, game duration and the date & time of the game's finish.
 */
public record PlayerStats(Integer score, String time, Instant finishedAt) implements Serializable, Comparable<PlayerStats> {
    @Override
    public int compareTo(PlayerStats o) {
        if (score.compareTo(o.score) != 0) {
            return -score.compareTo(o.score);
        } else {
            return time.compareTo(o.time);
        }
    }
}
