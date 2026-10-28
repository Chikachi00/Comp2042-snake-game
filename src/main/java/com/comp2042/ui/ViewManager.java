package com.comp2042.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/** Owns the menu and game windows and switches between them. */
public class ViewManager {
    private final Stage primaryStage;
    private final Image snakeLogo = new Image("resources/views/res/SnakeLogoWhite.png");
    private Stage gameStage;

    public ViewManager(Stage primaryStage) {
        this.primaryStage = Objects.requireNonNull(primaryStage, "primaryStage");
    }

    /** Loads the main menu once and shows the JavaFX primary stage. */
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

    Stage createGameStage() {
        gameStage = new Stage();
        gameStage.setTitle("Snake Game");
        gameStage.getIcons().add(snakeLogo);
        gameStage.setResizable(false);
        return gameStage;
    }

    void showGame(Scene gameScene) {
        gameStage.setScene(gameScene);
        gameStage.show();
        primaryStage.hide();
    }

    void showMainMenu() {
        primaryStage.show();
        if (gameStage != null) {
            gameStage.close();
            gameStage = null;
        }
    }
}
