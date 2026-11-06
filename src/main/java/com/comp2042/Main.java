package com.comp2042;

import javafx.application.Application;

/**
 * Launches JavaFX explicitly so Maven and IDE classpath launches share one entry point.
 */
public final class Main {

    /**
     * Prevents construction of the launcher utility.
     */
    private Main() {
    }

    /**
     * Starts the JavaFX application using an ordinary classpath entry point.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        Application.launch(SnakeApplication.class, args);
    }

}
