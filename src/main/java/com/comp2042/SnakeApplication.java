package com.comp2042;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Initializes the game's windows within the JavaFX application lifecycle.
 */
public class SnakeApplication extends Application {

    private static ViewManager viewManager;

    /**
     * Creates the window manager and displays the main menu.
     *
     * @param primaryStage the primary stage supplied by JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        viewManager = new ViewManager();
        viewManager.initalizeMainMenu();
    }

    /**
     * Returns the shared window manager after the application has started.
     *
     * @return the window manager, or {@code null} before application startup
     */
    public static ViewManager getViewManager() {
        return viewManager;
    }
}
