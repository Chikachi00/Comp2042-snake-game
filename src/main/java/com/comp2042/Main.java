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

    /**
     * Returns the window manager used by the running application.
     *
     * @return the window manager, or {@code null} before application startup
     */
    public static ViewManager getViewManager() {
        return SnakeApplication.getViewManager();
    }
}
