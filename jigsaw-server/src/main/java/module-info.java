module ru.hse.edu.vmpendischuk.jigsaw.server {
    requires ru.hse.edu.vmpendischuk.jigsaw.util;
    requires java.logging;
    requires java.sql;

    exports ru.hse.edu.vmpendischuk.jigsaw.server;
    exports ru.hse.edu.vmpendischuk.jigsaw.server.data;
    exports ru.hse.edu.vmpendischuk.jigsaw.server.game;
}