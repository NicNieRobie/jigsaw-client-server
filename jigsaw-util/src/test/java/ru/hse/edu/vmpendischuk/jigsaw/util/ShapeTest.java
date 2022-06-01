package ru.hse.edu.vmpendischuk.jigsaw.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Class that contains unit tests for the {@link Shape} class methods.
 */
class ShapeTest {
    /**
     * Checks if the shape's model matrix is validly returned for each of the type IDs.
     */
    @Test
    @DisplayName("Correctly returns shape model")
    void getsModel() {
        for (int i = 1; i <= 31; ++i) {
            Shape shape = new Shape("S" + i);
            int[][] model = shape.getModel();
            int[][] expectedModel = Shape.shapeModels.get("S" + i);
            assertTrue(Arrays.deepEquals(model, expectedModel));
        }
    }

    /**
     * Checks if a random shape is correctly generated.
     */
    @Test
    @DisplayName("Correctly generates a valid shape from the static shape models list")
    void getsRandomShape() {
        Shape shape = Shape.getRandomShape();
        assertTrue(Shape.shapeModels.containsKey(shape.typeId));
    }
}
