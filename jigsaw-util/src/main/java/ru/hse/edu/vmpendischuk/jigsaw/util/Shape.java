package ru.hse.edu.vmpendischuk.jigsaw.util;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

/**
 * Class that represents a shape in the Jigsaw game.
 * <p>
 * Shape is represented by its model - a 2D array (matrix),
 *   where 1 denotes a part of the shape.
 * <p>
 * The amount of shapes is limited - 31 shape models are used,
 *   each of them denoted with an <b>"S(num in range 1-31)"</b> key,
 *   called {@code typeId}.
 */
public class Shape implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    // A Random class instance used to generate random numbers.
    private static final Random random = new Random();
    // Map that stores models of shapes available for generation.
    public static HashMap<String, int[][]> shapeModels;
    static {
        // Initializing the map.
        shapeModels = new HashMap<>();
        shapeModels.put("S1", new int[][]{
                {1, 1},
                {1, 0},
                {1, 0}
        });
        shapeModels.put("S2", new int[][]{
                {1, 0, 0},
                {1, 1, 1}
        });
        shapeModels.put("S3", new int[][]{
                {0, 1},
                {0, 1},
                {1, 1}
        });
        shapeModels.put("S4", new int[][]{
                {1, 1, 1},
                {0, 0, 1}
        });
        shapeModels.put("S5", new int[][]{
                {1, 1},
                {0, 1},
                {0, 1}
        });
        shapeModels.put("S6", new int[][]{
                {0, 0, 1},
                {1, 1, 1}
        });
        shapeModels.put("S7", new int[][]{
                {1, 0},
                {1, 0},
                {1, 1}
        });
        shapeModels.put("S8", new int[][]{
                {1, 1, 1},
                {1, 0, 0}
        });
        shapeModels.put("S9", new int[][]{
                {1, 0},
                {1, 1},
                {0, 1}
        });
        shapeModels.put("S10", new int[][]{
                {0, 1, 1},
                {1, 1, 0}
        });
        shapeModels.put("S11", new int[][]{
                {0, 1},
                {1, 1},
                {1, 0}
        });
        shapeModels.put("S12", new int[][]{
                {1, 1, 0},
                {0, 1, 1}
        });
        shapeModels.put("S13", new int[][]{
                {0, 0, 1},
                {0, 0, 1},
                {1, 1, 1}
        });
        shapeModels.put("S14", new int[][]{
                {1, 0, 0},
                {1, 0, 0},
                {1, 1, 1}
        });
        shapeModels.put("S15", new int[][]{
                {1, 1, 1},
                {1, 0, 0},
                {1, 0, 0}
        });
        shapeModels.put("S16", new int[][]{
                {1, 1, 1},
                {0, 0, 1},
                {0, 0, 1}
        });
        shapeModels.put("S17", new int[][]{
                {0, 1, 0},
                {0, 1, 0},
                {1, 1, 1}
        });
        shapeModels.put("S18", new int[][]{
                {1, 1, 1},
                {0, 1, 0},
                {0, 1, 0}
        });
        shapeModels.put("S19", new int[][]{
                {1, 0, 0},
                {1, 1, 1},
                {1, 0, 0}
        });
        shapeModels.put("S20", new int[][]{
                {0, 0, 1},
                {1, 1, 1},
                {0, 0, 1}
        });
        shapeModels.put("S21", new int[][]{
                {1, 1, 1}
        });
        shapeModels.put("S22", new int[][]{
                {1},
                {1},
                {1}
        });
        shapeModels.put("S23", new int[][]{
                {1}
        });
        shapeModels.put("S24", new int[][]{
                {1, 1},
                {1, 0}
        });
        shapeModels.put("S25", new int[][]{
                {1, 1},
                {0, 1}
        });
        shapeModels.put("S26", new int[][]{
                {0, 1},
                {1, 1}
        });
        shapeModels.put("S27", new int[][]{
                {1, 0},
                {1, 1}
        });
        shapeModels.put("S28", new int[][]{
                {1, 0},
                {1, 1},
                {1, 0}
        });
        shapeModels.put("S29", new int[][]{
                {1, 1, 1},
                {0, 1, 0}
        });
        shapeModels.put("S30", new int[][]{
                {0, 1},
                {1, 1},
                {0, 1}
        });
        shapeModels.put("S31", new int[][]{
                {0, 1, 0},
                {1, 1, 1}
        });
    }

    // Shape's model type ID.
    public String typeId;

    /**
     * Initializes a new {@code Shape} instance with given model type ID.
     *
     * @param typeId shape model type ID.
     */
    public Shape(String typeId) {
        this.typeId = typeId;
    }

    /**
     * Returns shape's model matrix.
     *
     * @return shape's model matrix.
     */
    public int[][] getModel() {
        return shapeModels.get(typeId);
    }

    /**
     * Generates a shape with a random model type ID.
     *
     * @return the generated shape.
     */
    public static Shape getRandomShape() {
        int modelId = random.nextInt(32 - 1) + 1;
        return new Shape("S" + modelId);
    }
}
