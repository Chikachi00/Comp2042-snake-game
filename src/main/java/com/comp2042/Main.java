package com.comp2042;

import javafx.application.Application;

/**
 * Launches the game from either Maven or an IDE's standard Java run command.
 */
public final class Main {

    private Main() {
    }

    /**
     * Starts JavaFX explicitly so this entry point also works on the classpath.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        Application.launch(SnakeApplication.class, args);
    }

}
