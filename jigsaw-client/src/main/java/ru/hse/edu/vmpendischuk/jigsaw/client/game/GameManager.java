package ru.hse.edu.vmpendischuk.jigsaw.client.game;

import ru.hse.edu.vmpendischuk.jigsaw.client.network.DefaultJigsawClient;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.JigsawClient;
import ru.hse.edu.vmpendischuk.jigsaw.util.Shape;

/**
 * Class used for Jigsaw game state management.
 * <p>
 * Includes the game field state, game stats,
 *   shape that was generated for placement on the field
 *   and methods for shape update and placement.
 */
public class GameManager {
    // The client instance used to communicate with the server.
    private final JigsawClient client = DefaultJigsawClient.getInstance();

    // Shape that is to be placed on the field.
    public Shape currentShape;
    // Current state -of the game field.
    // 9x9 2D array, 1 = taken, 0 = free.
    public int[][] gameFieldState;
    // The amount of turns made by the player.
    public int turns;
    // Cells covered by shapes.
    public int coveredCells;
    public int shapesPlaced;

    /**
     * Initializes a new {@code GameManager} instance.
     */
    public GameManager() {
        // Initializing stats.
        turns = 0;
        coveredCells = 0;
        shapesPlaced = 0;

        // Initializing game field.
        gameFieldState = new int[9][9];
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                gameFieldState[i][j] = 0;
            }
        }
    }

    /**
     * Fetches the shape that is to be placed by the player
     *   and returns the flag that denotes if the shape has been fetched.
     */
    public boolean updateCurrentShape() {
        currentShape = client.getPlayerShape();

        return currentShape != null;
    }

    /**
     * Places the generated shape in the specified position on the field
     *   if the position is free.
     *
     * @param row index of the row of the game field in which the placement point is located.
     * @param col index of the column of the game field in which the placement point is located.
     * @param selectionRow index of the row in the shape model where the drag pivot is located.
     * @param selectionCol index of the column in the shape model where the drag pivot is located.
     */
    public void tryAddCurrentShape(int row, int col, int selectionRow, int selectionCol) {
        int[][] shapeModel = currentShape.getModel();
        turns += 1;

        // If the shape fits given the chosen point of placement.
        if (selectionRow <= row && selectionCol <= col &&
                shapeModel[0].length - selectionCol <= 9 - col &&
                shapeModel.length - selectionRow <= 9 - row) {
            // Checking if the required space is already taken.
            for (int i = col - selectionCol; i < col - selectionCol + shapeModel[0].length; ++i) {
                for (int j = row - selectionRow; j < row - selectionRow + shapeModel.length; ++j) {
                    if (shapeModel[j - row + selectionRow][i - col + selectionCol] == 1) {
                        if (gameFieldState[j][i] == 1) {
                            // Stop placing the shape if the space is taken.
                            return;
                        }
                    }
                }
            }

            shapesPlaced++;

            // Placing the shape in the free space.
            for (int i = col - selectionCol; i < col - selectionCol + shapeModel[0].length; ++i) {
                for (int j = row - selectionRow; j < row - selectionRow + shapeModel.length; ++j) {
                    if (shapeModel[j - row + selectionRow][i - col + selectionCol] == 1) {
                        gameFieldState[j][i] = 1;
                        coveredCells++;
                    }
                }
            }
        }
    }
}
