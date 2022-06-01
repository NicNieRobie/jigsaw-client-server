package ru.hse.edu.vmpendischuk.jigsaw.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.DefaultJigsawClient;
import ru.hse.edu.vmpendischuk.jigsaw.client.network.JigsawClient;

import java.io.IOException;

/**
 * Main JavaFX application class of the Jigsaw game.
 */
public class JigsawApplication extends Application {
    private static final JigsawClient client = DefaultJigsawClient.getInstance();

    /**
     * Application entry point.
     *
     * @param stage the main JavaFX stage used by application.
     * @throws IOException if the FXML view file could not be loaded.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(JigsawApplication.class.getResource("jigsaw-server-connection-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setResizable(false);
        stage.setTitle("Jigsaw");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Method called on application stop.
     */
    @Override
    public void stop() throws Exception {
        if (client.isConnected()) {
            client.disconnect();
        }

        super.stop();
    }

    /**
     * Program entry point.
     *
     * @param args launch arguments.
     */
    public static void main(String[] args) {
        launch();
    }
}