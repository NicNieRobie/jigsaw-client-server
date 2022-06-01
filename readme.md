## Jigsaw Client & Server + Derby

Author: *Vladislav Pendishchuk*

---

### Running

Two shell `.sh` scripts used to launch the game client
and the server are located in the **scripts** folder:
* **launch-client.sh** - is used to launch the client and has
  two variables:
  * `JAVA_HOME` - folder path to the Java SDK on the machine
    (must contain the bin folder);
  * `PATH_TO_FX` - folder path to the Java FX SDK (must contain the lib folder).
* **launch-server.sh** - is used to launch the server and has
  two variables:
  * `JAVA_HOME` - folder path to the Java SDK on the machine
    (must contain the bin folder);
  * `DERBY_DRIVER_PATH` - folder path to the Apache Derby
    driver jar files (must contain the lib folder).

### Configuration

The Jigsaw server will ask the used to specify the following configuration
information on startup:
* The port on which the server will serve the clients;
* The amount of players in the game;
* The maximum allowed duration of the game in seconds;
* The JDBC URL of an Embedded Derby database - if the database hasn't
  yet been created, specify `;create=true` in the end of the URL.

### Known issues

On MacOS (and several Linux distributions) the drag view (picture of a shape
being dragged) offset is too high, resulting in uncomfortable gameplay.
When placing a shape on the game field, remember that the pivot point
for the shape's placement is the square on which the player clicked
when 'picking' the shape 'up'.
