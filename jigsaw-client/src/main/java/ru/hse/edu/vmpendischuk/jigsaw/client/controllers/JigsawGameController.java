package ru.hse.edu.vmpendischuk.jigsaw.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.hse.edu.vmpendischuk.jigsaw.client.game.GameManager;
import ru.hse.edu.vmpendischuk.jigsaw.client.JigsawApplication;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.DefaultJigsawClient;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.JigsawClient;
import ru.hse.edu.vmpendischuk.jigsaw.client.time.StopWatch;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller class of the main Jigsaw game scene.
 * <p>
 * Contains handlers required for gameplay (drag-and-drop action handlers)
 *   as well as the Jigsaw game manager instance.
 */
public class JigsawGameController {
    private static final Logger logger = Logger.getLogger(JigsawGameController.class.getName());
    private final JigsawClient client = DefaultJigsawClient.getInstance();

    // GridPane used to display the game field.
    @FXML
    public GridPane grid;
    // GridPane used to display the next shape.
    @FXML
    public GridPane shapeGrid;
    // Label used to display the maximal allowed duration of the game.
    @FXML
    public Label maxDurationLabel;
    // Label used to display the amount of time passed since the start of the game.
    @FXML
    public Label timeLabel;
    // Label used to display the amount of shapes successfully placed by the used.
    @FXML
    public Label shapesLabel;
    // Label used to display the user's name.
    @FXML
    public Label usernameLabel;
    // Label used to display the opponent's name.
    @FXML
    public Label otherUsernameLabel;
    // The 'vs' label.
    @FXML
    public Label vsLabel;

    // GameManager instance used to manage the game.
    private final GameManager gameManager = new GameManager();
    // StopWatch instance used to measure the amount of time passed since the start.
    private StopWatch stopWatch;

    /**
     * Initializes a new {@code JigsawGameController} instance.
     */
    public JigsawGameController() {
        tryUpdateCurrentShape();
    }

    /**
     * Tries to get a new shape from the server and checks the server connection status.
     */
    private void tryUpdateCurrentShape() {
        if (!gameManager.updateCurrentShape()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Connection lost");

            alert.setHeaderText(null);
            alert.setContentText("Connection with the server has been lost, the game is over");

            alert.showAndWait();
            Platform.exit();
        }
    }

    /**
     * Initializes the {@code JigsawGameController} instance
     *   and the controlled view on scene load.
     */
    @FXML
    public void initialize() {
        // Starting the stopwatch.
        stopWatch = new StopWatch(timeLabel, client.getMaxDuration(), () -> {
            // Alert shown on game timeout.
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game timeout");

            alert.setHeaderText(null);
            alert.setContentText("The time is out, your game is over!");

            alert.show();

            // Switching to the result screen.
            try {
                goToGameResults(timeLabel.getScene());
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Could not load results view", ex);
            }
        });
        stopWatch.start();

        // Initializing labels.
        maxDurationLabel.setText(Integer.toString(client.getMaxDuration()));
        usernameLabel.setText(client.getUsername());

        String otherUsername = client.getOtherUsername();

        if (otherUsername == null) {
            otherUsernameLabel.setVisible(false);
            vsLabel.setVisible(false);
        } else {
            otherUsernameLabel.setVisible(true);
            vsLabel.setVisible(true);

            otherUsernameLabel.setText(otherUsername);
        }

        // Drawing the field.
        redraw();
    }

    /**
     * Shape drag action handler. Initializes the shape drag-and-drop action.
     *
     * @param event initialized mouse event.
     */
    @FXML
    public void onDragDetected(MouseEvent event) {
        // Initializing drag-and-drop on the shape render grid.
        Dragboard dragboard = shapeGrid.startDragAndDrop(TransferMode.ANY);
        Node targetNode = event.getPickResult().getIntersectedNode();

        if (targetNode != shapeGrid) {
            // Locating the target node in the grid.
            int col = GridPane.getColumnIndex(targetNode) == null ? 0 : GridPane.getColumnIndex(targetNode);
            int row = GridPane.getRowIndex(targetNode) == null ? 0 : GridPane.getRowIndex(targetNode);

            // Saving drag pivot coordinates in the dragboard.
            ClipboardContent content = new ClipboardContent();
            content.putString(row + ":" + col);
            dragboard.setContent(content);

            // Setting the drag view (displayed shape).
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            dragboard.setDragView(shapeGrid.snapshot(parameters, null), event.getX(), event.getY());
        }
    }

    /**
     * Game field grid drag over event handler.
     *
     * @param event initialized drag event.
     */
    @FXML
    public void onDragOver(DragEvent event) {
        if (event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }

        event.consume();
    }

    /**
     * Game field grid drag dropped event handler.
     *
     * @param event initialized drag event.
     */
    @FXML
    public void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasString() &&db.getString().contains(":")) {
            // Receiving the target cell.
            Node target = event.getPickResult().getIntersectedNode();

            // Retrieving drag pivot point coordinates.
            String cellId = db.getString();
            int rowId = Integer.parseInt(cellId.split(":", 2)[0]);
            int colId = Integer.parseInt(cellId.split(":", 2)[1]);

            // Retrieving target cell coordinates.
            int col = GridPane.getColumnIndex(target) == null ? 0 : GridPane.getColumnIndex(target);
            int row = GridPane.getRowIndex(target) == null ? 0 : GridPane.getRowIndex(target);

            // Trying to place the shape with given location parameters.
            gameManager.tryAddCurrentShape(row, col, rowId, colId);

            // Re-generating given shape.
            tryUpdateCurrentShape();

            // Redrawing both grids.
            redraw();
            event.setDropCompleted(true);
        } else {
            // If the dragboard does not contain required information.
            event.setDropCompleted(false);
        }
        event.consume();
    }

    /**
     * Redraws the game field grid and the shape display grid.
     */
    private void redraw() {
        grid.getChildren().clear();

        // Filling the game state grid.
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                Pane rect = new Pane();
                if (gameManager.gameFieldState[j][i] == 1) {
                    rect.getStyleClass().addAll("rectangle-pane", "active-rectangle-pane");
                } else {
                    rect.getStyleClass().addAll("rectangle-pane", "inactive-rectangle-pane");
                }
                rect.setMaxWidth(Double.MAX_VALUE);
                rect.setMaxHeight(Double.MAX_VALUE);
                GridPane.setFillWidth(rect, true);
                GridPane.setFillHeight(rect, true);
                grid.add(rect, i, j, 1, 1);
            }
        }

        shapeGrid.getChildren().clear();

        // Filling the shape display grid.
        int[][] currentShapeModel = gameManager.currentShape.getModel();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                if (j < currentShapeModel.length && i < currentShapeModel[0].length &&currentShapeModel[j][i] == 1) {
                    Pane rect = new Pane();
                    rect.getStyleClass().addAll("rectangle-pane", "active-rectangle-pane");
                    rect.setMaxWidth(Double.MAX_VALUE);
                    rect.setMaxHeight(Double.MAX_VALUE);
                    GridPane.setFillWidth(rect, true);
                    GridPane.setFillHeight(rect, true);
                    shapeGrid.add(rect, i, j);
                }
            }
        }

        shapesLabel.setText("Score: " + gameManager.shapesPlaced);
    }

    /**
     * Finish button click handler.
     *
     * @param event initialized action event.
     * @throws IOException if the result view FXML could not be loaded.
     */
    @FXML
    public void finishButtonClickHandler(ActionEvent event) throws IOException {
        goToGameResults(((Node)event.getSource()).getScene());
    }

    /**
     * Initializes and switches to the Jigsaw game results screen.
     *
     * @param scene current scene used to get the primary stage.
     * @throws IOException if the result view FXML could not be loaded.
     */
    private void goToGameResults(Scene scene) throws IOException {
        // Initializing the result scene.
        Stage stage = (Stage) scene.getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(JigsawApplication.class.getResource("jigsaw-results-view.fxml"));
        Scene newScene = new Scene(fxmlLoader.load());
        stage.setScene(newScene);
        JigsawResultsController controller = fxmlLoader.getController();
        controller.initializeData(timeLabel.getText(), gameManager.shapesPlaced);

        stopWatch.reset();
        stage.show();
    }
}