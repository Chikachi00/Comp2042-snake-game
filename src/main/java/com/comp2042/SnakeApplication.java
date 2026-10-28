package com.comp2042;

import com.comp2042.ui.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

/** Initializes the game windows within the JavaFX application lifecycle. */
public class SnakeApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        ViewManager viewManager = new ViewManager(primaryStage);
        viewManager.initializeMainMenu();
    }
}
