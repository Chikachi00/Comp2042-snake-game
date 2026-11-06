package com.comp2042;

import com.comp2042.ui.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Initializes the main menu within the JavaFX application lifecycle.
 */
public class SnakeApplication extends Application {
    /**
     * Creates and shows the main menu on the JavaFX application thread.
     *
     * @param primaryStage JavaFX stage to initialize or resize
     */
    @Override
    public void start(Stage primaryStage) {
        ViewManager viewManager = new ViewManager(primaryStage);
        viewManager.initializeMainMenu();
    }
}
