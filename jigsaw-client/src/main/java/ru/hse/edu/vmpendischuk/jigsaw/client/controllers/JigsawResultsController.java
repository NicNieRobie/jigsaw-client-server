package ru.hse.edu.vmpendischuk.jigsaw.client.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.hse.edu.vmpendischuk.jigsaw.client.JigsawApplication;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.*;
import ru.hse.edu.vmpendischuk.jigsaw.util.GameResults;
import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStatsEntry;

import java.io.IOException;

/**
 * Controller class of the main Jigsaw game results scene.
 */
public class JigsawResultsController {
    // The client instance used to communicate with the server.
    private final JigsawClient client = DefaultJigsawClient.getInstance();

    // Label used to display the winner's name.
    @FXML
    public Label winnerLabel;
    // Label used to display the names of disconnected players.
    @FXML
    public Label disconnectedPlayersLabel;
    // Label used to display server connection errors.
    @FXML
    public Label errorLabel;
    // VBox that serves as a container for the "waiting for other players to finish" loading screen.
    @FXML
    public VBox finishLoadingVBox;
    // VBox that serves as a container for the "waiting for other players" loading screen.
    @FXML
    public VBox waitingLoadingVBox;
    // Table view used to display the game results.
    @FXML
    public TableView<PlayerStatsEntry> resultsTable;
    // Table column used to display usernames.
    @FXML
    public TableColumn<PlayerStatsEntry, String> colUsername;
    // Table column used to display the scores of players.
    @FXML
    public TableColumn<PlayerStatsEntry, Integer> colScore;
    // Table column used to display the time it took for the player to finish.
    @FXML
    public TableColumn<PlayerStatsEntry, String> colTime;
    // List used to store data for the table view.
    private final ObservableList<PlayerStatsEntry> data = FXCollections.observableArrayList();

    /**
     * Initializes the Jigsaw game results scene with given data.
     *
     * @param time time elapsed since the start of the game to its finish.
     * @param shapeCount the amount of shapes successfully placed by the player.
     */
    public void initializeData(String time, int shapeCount) {
        // Setting up the table view.
        colUsername.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().username()));
        colScore.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().score()).asObject());
        colTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().time()));
        disconnectedPlayersLabel.setVisible(false);

        // Finishing the game for current player.
        new Thread(() -> client.finishGameForPlayer(shapeCount, time, new GameFinishCallbackHandler() {
            @Override
            public void onSuccess(GameResults gameResults) {
                Platform.runLater(() -> {
                    // Filling the table.
                    data.addAll(gameResults.stats());
                    resultsTable.setItems(data);

                    // Displaying the winner's name.
                    winnerLabel.setText("Winner: " + gameResults.winner());

                    // Showing disconnected players.
                    if (gameResults.disconnectedPlayers().isEmpty()) {
                        disconnectedPlayersLabel.setVisible(false);
                    } else {
                        disconnectedPlayersLabel.setVisible(true);
                        disconnectedPlayersLabel.setText("Disconnected players: "
                                + String.join(", ", gameResults.disconnectedPlayers()));
                    }

                    finishLoadingVBox.setVisible(false);
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Displaying an alert with error.
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Connection failed");

                    alert.setHeaderText(null);
                    alert.setContentText("Could not load game results from the server!");

                    alert.showAndWait();
                });
            }
        })).start();
    }

    /**
     * New game button click handler.
     */
    @FXML
    public void newGameButtonClickHandler() {
        // Enabling the loading screen.
        waitingLoadingVBox.setVisible(true);
        errorLabel.setVisible(false);

        // Restarting the game.
        new Thread(() -> client.restart(new CallbackHandler() {
            @Override
            public void onSuccess() {
                // Staring the gam when all players have joined.
                Platform.runLater(() -> {
                    waitingLoadingVBox.setVisible(false);
                    startGame(waitingLoadingVBox.getScene());
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Displaying the error.
                Platform.runLater(() -> {
                    errorLabel.setVisible(true);
                    errorLabel.setText("ERROR: " + errorMessage);
                });
            }
        })).start();
    }

    /**
     * Top 10 games button click handler.
     */
    @FXML
    public void topGamesButtonClickHandler() {
        // Opening a new window with top 10 games listed.
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(JigsawApplication.class.getResource("jigsaw-top-view.fxml"));
            Scene newScene = new Scene(fxmlLoader.load());
            stage.setScene(newScene);
            JigsawTopController controller = fxmlLoader.getController();
            controller.initializeData();

            stage.show();
        } catch (IOException ex) {
            System.out.println("Could not load the top games screen");
        }
    }

    /**
     * Exit button click handler.
     */
    @FXML
    public void exitButtonClickHandler() {
        Platform.exit();
    }

    /**
     * Starts a game and switches to the main game screen.
     *
     * @param scene current scene used to get the primary stage.
     */
    private void startGame(Scene scene) {
        try {
            // Initializing the main Jigsaw game scene.
            Stage stage = (Stage) scene.getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(JigsawApplication.class.getResource("jigsaw-view.fxml"));
            Scene newScene = new Scene(fxmlLoader.load());
            stage.setScene(newScene);

            stage.show();
        } catch (IOException ex) {
            System.out.println("Could not load the game screen");
        }
    }
}
