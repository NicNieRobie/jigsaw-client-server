package ru.hse.edu.vmpendischuk.jigsaw.client.time;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.TimeoutHandler;

/**
 * Class used to measure time elapsed since the watch has started.
 * <p>
 * Requires a {@link Label} object to output and update displayed time.
 */
public class StopWatch {
    // Label used to display the elapsed time.
    private final Label timeLabel;
    // The game's maximum allowed duration.
    private final int maxDuration;
    // The handler used to handle the game timeout.
    private final TimeoutHandler timeoutHandler;
    private int totalSeconds;
    // Amount of hours passed.
    private int hours;
    // Amount of minutes passed in current hour.
    private int minutes;
    // Amount of seconds passed in current minute.
    private int seconds;
    // Timeline object used to automatically update time.
    private Timeline timeline;

    /**
     * Initializes a {@code StopWatch} instance.
     *
     * @param timeLabel label used to display the elapsed time.
     */
    public StopWatch(Label timeLabel, int maxDuration, TimeoutHandler timeoutHandler) {
        hours = 0;
        minutes = 0;
        seconds = 0;
        totalSeconds = 0;
        this.timeLabel = timeLabel;
        this.timeoutHandler = timeoutHandler;
        this.maxDuration = maxDuration;
    }

    /**
     * Starts the stopwatch (starts the time measurement).
     */
    public void start() {
        // Keyframe that updates time every second.
        KeyFrame keyFrame = new KeyFrame(Duration.millis(1000), e -> {
            seconds++;
            totalSeconds++;
            updateTime();
        });

        // Starting the timeline loop.
        timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Resets the stopwatch's measurements and stops it.
     */
    public void reset() {
        timeline.stop();
        totalSeconds = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        updateTime();
    }

    /**
     * Updates displayed time.
     */
    public void updateTime() {
        if (seconds == 60) {
            seconds = 0;
            minutes++;
        }

        if (minutes == 60) {
            minutes = 0;
            hours++;
        }

        String h = hours >= 10 ? String.valueOf(hours) : "0" + hours;
        String m = minutes >= 10 ? String.valueOf(minutes) : "0" + minutes;
        String s = seconds >= 10 ? String.valueOf(seconds) : "0" + seconds;

        timeLabel.setText(h + ":" + m + ":" + s);

        // On game timeout.
        if (totalSeconds == maxDuration) {
            timeoutHandler.onTimeout();
        }
    }
}
