package ru.hse.edu.vmpendischuk.jigsaw.client.game;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.hse.edu.vmpendischuk.jigsaw.util.Shape;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Class that contains unit tests for the {@link GameManager} class methods.
 */
class GameManagerTest {
    /**
     * Tests if the current shape can be correctly added onto the game field
     *   if the specified position is free.
     */
    @Test
    @DisplayName("Correctly adds current shape to the game field in the given position if it's free")
    void addsCurrentShapeWhenFree() {
        GameManager gameManager = new GameManager();

        // Adding a shape.
        gameManager.currentShape = new Shape("S2");
        gameManager.tryAddCurrentShape(8, 0, 1, 0);

        int[][] expectedState = new int[][] {
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 0, 0, 0, 0, 0, 0}
        };

        // Checking the game field state.
        assertTrue(Arrays.deepEquals(expectedState, gameManager.gameFieldState));
    }

    /**
     * Tests if the current shape is not added onto the game field
     *   if the specified position is not free.
     */
    @Test
    @DisplayName("Does not add current shape to the game field in the given position if it's not free")
    void doesNotAddCurrentShapeWhenNotFree() {
        GameManager gameManager = new GameManager();

        // Placing a shape in the free position.
        gameManager.currentShape = new Shape("S2");
        gameManager.tryAddCurrentShape(8, 0, 1, 0);

        // Placing a shape in the taken position.
        gameManager.tryAddCurrentShape(7, 0, 1, 0);

        int[][] expectedState = new int[][] {
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 0, 0, 0, 0, 0, 0}
        };

        // Checking the game field state.
        assertTrue(Arrays.deepEquals(expectedState, gameManager.gameFieldState));
    }
}
