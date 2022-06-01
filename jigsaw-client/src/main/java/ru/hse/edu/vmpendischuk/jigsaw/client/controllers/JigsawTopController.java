package ru.hse.edu.vmpendischuk.jigsaw.client.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.DefaultJigsawClient;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.JigsawClient;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.TopGamesCallbackHandler;
import ru.hse.edu.vmpendischuk.jigsaw.util.PlayerStatsEntry;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JigsawTopController {
    // The client instance used to communicate with the server.
    private final JigsawClient client = DefaultJigsawClient.getInstance();

    // Label used to display server connection errors.
    @FXML
    public Label errorLabel;
    // VBox that serves as a container for the loading screen.
    @FXML
    public VBox loadingVBox;
    // Table view used to display the top 10 game results.
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
    // Table column used to display the time at which the game was finished.
    @FXML
    public TableColumn<PlayerStatsEntry, String> colFinishedAt;
    // List used to store data for the table view.
    ObservableList<PlayerStatsEntry> data = FXCollections.observableArrayList();

    /**
     * Initializes the Jigsaw top 10 game results scene.
     */
    public void initializeData() {
        // Setting up the table view.
        colUsername.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().username()));
        colScore.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().score()).asObject());
        colTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().time()));
        colFinishedAt.setCellValueFactory(cellData -> {
            OffsetDateTime dateTime = OffsetDateTime.ofInstant(cellData.getValue().finishedAt(), ZoneId.systemDefault());
            String stringDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
            return new SimpleStringProperty(stringDate);
        });

        // Enabling the loading screen.
        loadingVBox.setVisible(true);
        errorLabel.setVisible(false);

        // Fetching the top 10 games list from the server.
        new Thread(() -> client.getTopGames(new TopGamesCallbackHandler() {
            @Override
            public void onSuccess(List<PlayerStatsEntry> topGames) {
                // Displaying the games.
                Platform.runLater(() -> {
                    loadingVBox.setVisible(false);

                    data.addAll(topGames);
                    resultsTable.setItems(data);
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Displaying the error message.
                Platform.runLater(() -> {
                    loadingVBox.setVisible(false);
                    errorLabel.setVisible(true);
                    errorLabel.setText(errorMessage);
                });
            }
        })).start();
    }
}
