## About This Project

This is the Server Side Interface for the Java TP's Trajectory Sending application.

The TP is about transmitting the Traject designed in the client side to the serveur side which need to rebuild it.
this can be used to make a game for example where the server player can see the chicken and the its egg(destination) as well as the obstacles between them, but the client player cannot, they can only see the chicken itself as well as some other obstacles not seen by the server player, then using the Chat app in this folder, they can communicate in real-time to discuss and describe what to do so that the client can draw the right trajectory to the egg.

## How to Run this App

To run this app you need to have javafx installed, then you will have to run this command while substituting the paths with the correct paths in your system:

- install javafx (of the same version as your jdk) in a folder /path/to/javafx
- use the terminal to access this project's folder
- cd into the src folder
- run the fallowing command :<br>
```
java --module-path=/path/to/javafx/lib --add-modules=javafx.base,javafx.controls,javafx.graphics App.java
```

## Folder Structure

The workspace contains three folders, where:

- `src`: the folder to Contain sources
- `lib`: the folder to maintain dependencies
- `bin`: the folder to contain Compiled Binaries

## Students Involved:

-   *ZAKI OUSSAMA*
-   *ZIENTECKI MORANN*
