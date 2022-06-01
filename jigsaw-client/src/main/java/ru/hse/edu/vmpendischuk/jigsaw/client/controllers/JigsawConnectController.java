package ru.hse.edu.vmpendischuk.jigsaw.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.hse.edu.vmpendischuk.jigsaw.client.JigsawApplication;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.*;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.CallbackHandler;

import java.io.IOException;

/**
 * Controller class of the Jigsaw game configuration scene.
 * <p>
 * Contains handlers required for connecting to the server,
 *   player registration and starting the game.
 */
public class JigsawConnectController {
    // The client instance used to communicate with the server.
    private final JigsawClient client = DefaultJigsawClient.getInstance();

    // Label used to display a server connection error.
    @FXML
    public Label connectionErrorLabel;
    // Label used to display a registration error.
    @FXML
    public Label registerErrorLabel;
    // Field used for entering the server IP address or host.
    @FXML
    public TextField ipField;
    // Field used for entering the server port.
    @FXML
    public TextField portField;
    // Field used for entering the username.
    @FXML
    public TextField usernameField;
    // VBox that serves as a container for the "waiting for the connection" loading screen.
    @FXML
    public VBox connectionLoadingVBox;
    // VBox that serves as a container for the "waiting for other players" loading screen.
    @FXML
    public VBox playerLoadingVBox;
    // VBox that serves as a container for registration UI controls.
    @FXML
    public VBox welcomeVBox;
    // VBox that server as a container for game start and exit buttons.
    @FXML
    public HBox continueHBox;

    /**
     * Connect button click handler.
     */
    @FXML
    public void connectButtonClickHandler() {
        // Resetting the screen state.
        connectionErrorLabel.setVisible(false);
        playerLoadingVBox.setVisible(false);
        connectionLoadingVBox.setVisible(false);
        welcomeVBox.setVisible(false);
        continueHBox.setVisible(false);

        int port;

        // Trying to parse the port.
        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException ex) {
            connectionErrorLabel.setText("Port has to be a number");
            connectionErrorLabel.setVisible(true);
            return;
        }

        // Enabling the loading screen.
        connectionLoadingVBox.setVisible(true);

        // Connecting to the server.
        new Thread(() -> client.connect(ipField.getText(), port, new CallbackHandler() {
            @Override
            public void onSuccess() {
                // Switching to the registration controls.
                Platform.runLater(() -> {
                    connectionLoadingVBox.setVisible(false);

                    welcomeVBox.setVisible(true);
                    continueHBox.setVisible(true);
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Displaying the connection error.
                Platform.runLater(() -> {
                    connectionLoadingVBox.setVisible(false);

                    connectionErrorLabel.setText(errorMessage);
                    connectionErrorLabel.setVisible(true);
                });
            }
        })).start();
    }

    /**
     * Continue button click handler.
     *
     * @param event initialized action event.
     */
    @FXML
    public void continueButtonClickHandler(ActionEvent event) {
        // Resetting the screen state.
        registerErrorLabel.setVisible(false);
        playerLoadingVBox.setVisible(false);
        connectionLoadingVBox.setVisible(false);

        // Checking the username for validity.
        if (usernameField.getText().isBlank()) {
            registerErrorLabel.setText("Username can not be blank");
            registerErrorLabel.setVisible(true);
            return;
        }

        // Enabling the loading screen.
        playerLoadingVBox.setVisible(true);

        // Registering player on the server.
        new Thread(() -> client.registerPlayer(usernameField.getText(), new CallbackHandler() {
            @Override
            public void onSuccess() {
                // Starting the game.
                Platform.runLater(() -> {
                    playerLoadingVBox.setVisible(false);

                    startGame(((Node) event.getSource()).getScene());
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Displaying the registration error.
                Platform.runLater(() -> {
                    registerErrorLabel.setText(errorMessage);
                    registerErrorLabel.setVisible(true);
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
     * Initializes and switches to the main Jigsaw game screen.
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

    /**
     * Exit button click handler.
     */
    @FXML
    public void exitButtonClickHandler() {
        Platform.exit();
    }
}
