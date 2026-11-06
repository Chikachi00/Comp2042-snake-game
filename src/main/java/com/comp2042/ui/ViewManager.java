package com.comp2042.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Creates game windows and switches between the main menu and game stage.
 */
public class ViewManager {
    private final Stage primaryStage;
    private final Image snakeLogo = new Image("resources/views/res/SnakeLogoWhite.png");
    private Stage gameStage;

    /**
     * Stores the primary stage used to display the main menu.
     *
     * @param primaryStage JavaFX stage to initialize or resize
     */
    public ViewManager(Stage primaryStage) {
        this.primaryStage = Objects.requireNonNull(primaryStage, "primaryStage");
    }

    /**
     * Loads FXML with an injected controller and shows the main menu.
     */
    public void initializeMainMenu() {
        FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource("/resources/views/MainMenu.fxml"));
        loader.setControllerFactory(type -> {
            if (type == MainMenuController.class) {
                return new MainMenuController(this);
            }
            throw new IllegalArgumentException("Unsupported FXML controller: " + type.getName());
        });
        final AnchorPane root;
        try {
            root = loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load the main menu", exception);
        }
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(snakeLogo);
        primaryStage.setTitle("Snake Game");
        primaryStage.show();
    }

    /**
     * Creates and stores a new game stage.
     *
     * @return the newly created game stage
     */
    Stage createGameStage() {
        gameStage = new Stage();
        gameStage.setTitle("Snake Game");
        gameStage.getIcons().add(snakeLogo);
        gameStage.setResizable(false);
        return gameStage;
    }

    /**
     * Shows the game scene and hides the main menu.
     *
     * @param gameScene scene containing the playable game
     */
    void showGame(Scene gameScene) {
        gameStage.setScene(gameScene);
        gameStage.show();
        primaryStage.hide();
    }

    /**
     * Shows the main menu and closes the current game stage.
     */
    void showMainMenu() {
        primaryStage.show();
        if (gameStage != null) {
            gameStage.close();
            gameStage = null;
        }
    }
}
