module ru.hse.edu.vmpendischuk.jigsaw.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires ru.hse.edu.vmpendischuk.jigsaw.util;

    exports ru.hse.edu.vmpendischuk.jigsaw.client;
    exports ru.hse.edu.vmpendischuk.jigsaw.client.network;
    opens ru.hse.edu.vmpendischuk.jigsaw.client.network to javafx.fxml;
    exports ru.hse.edu.vmpendischuk.jigsaw.client.time;
    opens ru.hse.edu.vmpendischuk.jigsaw.client.time to javafx.fxml;
    exports ru.hse.edu.vmpendischuk.jigsaw.client.controllers;
    opens ru.hse.edu.vmpendischuk.jigsaw.client.controllers to javafx.fxml;
    exports ru.hse.edu.vmpendischuk.jigsaw.client.game;
    opens ru.hse.edu.vmpendischuk.jigsaw.client.game to javafx.fxml;
}