package com.comp2042.ui;

import com.comp2042.game.Coin;
import com.comp2042.game.CoinType;
import com.comp2042.game.Direction;
import com.comp2042.game.Game;
import com.comp2042.game.GameSession;
import com.comp2042.game.Obstacle;
import com.comp2042.game.Snake;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;

import java.awt.*;
import java.util.Map;
import java.util.List;

/**
 * Draws board sprites and presents progress labels, pause menus and end menus.
 */
public class GameView {

    private Canvas gridCanvas;
    private GraphicsContext gc;

    private Label gameOverLabel;
    private Label gameWonLabel;
    private Label scoreLabel;
    private Label progressLabel;
    private Label comboLabel;
    private Label startHint;
    private VBox menu;
    private Label menuTitle;
    private Label menuDetails;
    private Button resumeButton;
    private Button restartButton;
    private Button mainMenuButton;
    private boolean gameOverVis = false;
    private boolean gameWonVis = false;
    private final ImageView imageHead = new ImageView(new Image("resources/views/res/snakehead.png"));
    private final ImageView imageBody = new ImageView(new Image("resources/views/res/snakebody.png"));
    private final ImageView imageTurn = new ImageView(new Image("resources/views/res/snaketurnleft.png"));
    private final ImageView imageTail = new ImageView(new Image("resources/views/res/snaketail.png"));
    private final Map<CoinType, Image> coinImages = Map.of(
            CoinType.NORMAL, new Image("resources/views/res/redcoin.png"),
            CoinType.GOLD, new Image("resources/views/res/goldcoin.png"),
            CoinType.RAINBOW, new Image("resources/views/res/rainbowcoin.png"),
            CoinType.BLUE, new Image("resources/views/res/bluecoin.png"),
            CoinType.SILVER, new Image("resources/views/res/silvercoin.png")
    );

    private final Game game;
    private double initX;
    private double initY;
    private double fullScale = 1;
    private boolean fullsize = false;
    private final int[][] turnArray;

    /**
     * Creates sprite resources and a turn map for the supplied game.
     *
     * @param game game settings and score state
     */
    public GameView(Game game) {
        this.game = game;
        this.turnArray = new int[game.getWidth()][game.getHeight()];
    }

    /**
     * Creates the board canvas, status labels and menu controls for a scene.
     *
     * @param scene scene defining the initial canvas dimensions
     * @param snek current snake
     * @param coin current or collected coin
     */
    public void makeScene(Scene scene, Snake snek, Coin coin) {
        gridCanvas = new Canvas(scene.getWidth(), scene.getHeight());
        initX = scene.getWidth();
        initY = scene.getHeight();
        gc = gridCanvas.getGraphicsContext2D();
        drawBoardContents(coin, snek);

        gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setId("gameOverLabel");
        String css = "resources/views/styling.css";
        gameOverLabel.getStylesheets().add(css);
        gameOverLabel.setVisible(false);

        gameWonLabel = new Label("YOU WON!");
        gameWonLabel.setId("gameWonLabel");
        gameWonLabel.getStylesheets().add(css);
        gameWonLabel.setVisible(false);

        scoreLabel = new Label("Score: " + game.getScorePoints());
        scoreLabel.setId("scoreLabel");
        scoreLabel.getStylesheets().add(css);
        scoreLabel.setFont(new Font("Arial", 20));
        scoreLabel.setVisible(true);
        progressLabel = new Label();
        progressLabel.setId("progressLabel");
        progressLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #1c2d20;");
        comboLabel = new Label();
        comboLabel.setId("comboLabel");
        comboLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #1c2d20;");
        startHint = new Label("Press an arrow key to start");
        startHint.setId("startHint");
        startHint.setStyle("-fx-font-size: 15; -fx-text-fill: #304d32; -fx-background-color: rgba(255,255,245,0.85); -fx-padding: 8;");
        startHint.setMouseTransparent(true);

        menuTitle = new Label();
        menuDetails = new Label();
        resumeButton = new Button("Resume");
        restartButton = new Button("Restart");
        mainMenuButton = new Button("Main Menu");
        resumeButton.setId("resumeButton");
        restartButton.setId("restartButton");
        mainMenuButton.setId("mainMenuButton");
        menu = new VBox(12, menuTitle, menuDetails, resumeButton, restartButton, mainMenuButton);
        menu.setId("gameMenu");
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: rgba(20, 24, 30, 0.88); -fx-padding: 18;");
        menuTitle.setStyle("-fx-text-fill: white; -fx-font-size: 25;");
        menuDetails.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        hideMenu();
    }

    /**
     * Connects the resume, restart and main-menu buttons to controller actions.
     *
     * @param resume action for the Resume button
     * @param restart action for the Restart button
     * @param mainMenu action for the Main Menu button
     */
    public void setMenuActions(Runnable resume, Runnable restart, Runnable mainMenu) {
        resumeButton.setOnAction(event -> resume.run());
        restartButton.setOnAction(event -> restart.run());
        mainMenuButton.setOnAction(event -> mainMenu.run());
    }

    /**
     * Shows all pause-menu actions.
     */
    public void showPauseMenu() {
        menuTitle.setText("Paused");
        menuDetails.setText("P / Esc: Resume     Tab: Restart");
        resumeButton.setVisible(true);
        resumeButton.setManaged(true);
        menu.setManaged(true);
        menu.setVisible(true);
    }

    /**
     * Shows the final score with restart and main-menu actions.
     *
     * @param won whether the run ended in victory
     */
    public void showEndMenu(boolean won) {
        gameOverLabel.setVisible(false);
        gameWonLabel.setVisible(false);
        menuTitle.setText(won ? "You won!" : "Game over");
        menuDetails.setText("Final score: " + game.getScorePoints());
        resumeButton.setVisible(false);
        resumeButton.setManaged(false);
        menu.setManaged(true);
        menu.setVisible(true);
    }

    /**
     * Hides the menu and removes it from layout calculations.
     */
    public void hideMenu() {
        menu.setVisible(false);
        menu.setManaged(false);
    }

    /**
     * Shows the prompt to start or continue with an arrow key.
     */
    public void showStartHint() { startHint.setVisible(true); }
    /**
     * Hides the arrow-key start prompt.
     */
    public void hideStartHint() { startHint.setVisible(false); }

    /**
     * Clears turn sprites left by an earlier snake.
     */
    public void resetTurns() {
        for (int[] column : turnArray) java.util.Arrays.fill(column, 0);
    }

    /**
     * Adds the canvas, HUD, hint and menus to the game pane in drawing order.
     *
     * @param gamePane pane that will contain board and interface controls
     */
    public void attachTo(StackPane gamePane) {
        gamePane.getChildren().addAll(gridCanvas, gameOverLabel, gameWonLabel, scoreLabel, progressLabel, comboLabel, startHint, menu);
        gamePane.setPadding(new Insets(20));
        StackPane.setAlignment(gameOverLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(gameWonLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(scoreLabel, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(progressLabel, Pos.TOP_LEFT);
        StackPane.setAlignment(comboLabel, Pos.TOP_RIGHT);
    }

    /**
     * Resizes the board and redraws its current contents to match the window's fullscreen state.
     *
     * @param fullScreen whether to enter fullscreen or restore windowed dimensions
     * @param obstacle board obstacle collection
     * @param snek current snake
     * @param coin current or collected coin
     */
    public void resizeForFullscreen(boolean fullScreen, Obstacle obstacle, Snake snek, Coin coin) {
        double xScaling = Screen.getPrimary().getVisualBounds().getHeight() / initY;
        double yScaling = Screen.getPrimary().getVisualBounds().getWidth() / initX;

        if (fullScreen && !fullsize) {
            if (xScaling < yScaling) {
                fullScale = xScaling;
            } else {
                fullScale = yScaling;
            }
            gridCanvas.setWidth(gridCanvas.getWidth() * fullScale);
            gridCanvas.setHeight(gridCanvas.getHeight() * fullScale);
            fullsize = true;
        } else if (!fullScreen) {
            fullScale = 1;
            gridCanvas.setWidth(initX);
            gridCanvas.setHeight(initY);
            fullsize = false;
        }

        gameOverLabel.setFont(new Font("Arial", 25 * fullScale));
        gameWonLabel.setFont(new Font("Arial", 25 * fullScale));
        drawGrid(coin, snek, obstacle);
        gameOverLabel.setStyle(fullScreen ? "-fx-font-size: 50;" : "-fx-font-size: 15;");
        gameWonLabel.setStyle(fullScreen ? "-fx-font-size: 50;" : "-fx-font-size: 15;");
        scoreLabel.setStyle(fullScreen ? "-fx-font-size: 35;" : "-fx-font-size: 15;");
    }

    /**
     * Redraws the board, coin, snake and obstacles.
     *
     * @param coin current or collected coin
     * @param snek current snake
     * @param obstacle board obstacle collection
     */
    public void drawGrid(Coin coin, Snake snek, Obstacle obstacle) {
        drawBoardContents(coin, snek);
        gc.setFill(obstacle.getColor());
        for (Point point : obstacle.getObstacles()) {
            gc.fillRect(point.x * game.getMultiplier() * fullScale, point.y * game.getMultiplier() * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
        }
    }

    /**
     * Clears the canvas and draws the coin and snake.
     *
     * @param coin current or collected coin
     * @param snek current snake
     */
    private void drawBoardContents(Coin coin, Snake snek) {
        gc.setFill(game.getColor());
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        gc.drawImage(coinImages.get(coin.getType()), (coin.getPos().x * game.getMultiplier()) * fullScale, (coin.getPos().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale - 3 * fullScale, game.getMultiplier() * fullScale - 3 * fullScale);

        List<Point> body = snek.getBody();
        int bodyIndex = 0;
        for (Point point : body) {
            drawBody(snek, body, point, bodyIndex);
            bodyIndex++;
        }

        drawHead(snek);
    }

    /**
     * Converts a movement direction to a sprite rotation.
     *
     * @param direction requested movement direction
     * @return the clockwise rotation in degrees
     */
    private static int rotation(Direction direction) {
        return switch (direction) {
            case UP -> 0;
            case RIGHT -> 90;
            case DOWN -> 180;
            case LEFT -> 270;
        };
    }

    /**
     * Draws the rotated snake head at one scaled cell size.
     *
     * @param snek current snake
     */
    private void drawHead(Snake snek) {
        imageHead.setRotate(rotation(snek.getDirection()));
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image rotatedImage = imageHead.snapshot(params, null);
        gc.drawImage(rotatedImage, (snek.getHead().x * game.getMultiplier()) * fullScale, (snek.getHead().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
    }

    /**
     * Draws a tail, corner or straight segment at one scaled cell size.
     *
     * @param snek current snake
     * @param body tail-to-head body coordinates
     * @param point board coordinate of the segment to draw
     * @param index index of the body segment
     */
    private void drawBody(Snake snek, List<Point> body, Point point, int index) {
        Image rotatedImage;
        if (body.size() <= 1) {
            imageTail.setRotate(rotation(snek.getDirection()));
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = imageTail.snapshot(params, null);
        } else if (index == 0) {
            int rotateTail;
            if (body.get(1).getX() == body.get(0).getX() && body.get(1).getY() == body.get(0).getY() - 1) {
                rotateTail = 0;
            } else if (body.get(1).getX() == body.get(0).getX() && body.get(1).getY() == body.get(0).getY() + 1) {
                rotateTail = 180;
            } else if (body.get(1).getX() == body.get(0).getX() - 1 && body.get(1).getY() == body.get(0).getY()) {
                rotateTail = 270;
            } else {
                rotateTail = 90;
            }
            imageTail.setRotate(rotateTail);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = imageTail.snapshot(params, null);
        } else if (index > 0 && (turnArray[(int) body.get(index).getX()][(int) body.get(index).getY()] != 0)) {
            imageTurn.setRotate(90 * turnArray[(int) body.get(index).getX()][(int) body.get(index).getY()]);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = imageTurn.snapshot(params, null);
        } else {
            imageBody.setRotate(rotation(snek.getBodyDirection(index)));
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = imageBody.snapshot(params, null);
        }

        if (turnArray[(int) body.get(0).getX()][(int) body.get(0).getY()] != 0) {
            turnArray[(int) body.get(0).getX()][(int) body.get(0).getY()] = 0;
        }

        gc.drawImage(rotatedImage, (point.x * game.getMultiplier()) * fullScale, (point.y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
    }

    /**
     * Records a corner sprite at the head when its requested direction changes.
     *
     * @param snek current snake
     * @param lastKey previous accepted direction
     * @param key new accepted direction
     */
    public void turnDir(Snake snek, Direction lastKey, Direction key) {
        if ((lastKey == Direction.LEFT && key == Direction.UP) || (lastKey == Direction.DOWN && key == Direction.RIGHT)) {
            turnArray[(int) snek.getHead().getX()][(int) snek.getHead().getY()] = 2;
        } else if ((lastKey == Direction.LEFT && key == Direction.DOWN) || (lastKey == Direction.UP && key == Direction.RIGHT)) {
            turnArray[(int) snek.getHead().getX()][(int) snek.getHead().getY()] = 3;
        } else if ((lastKey == Direction.RIGHT && key == Direction.UP) || (lastKey == Direction.DOWN && key == Direction.LEFT)) {
            turnArray[(int) snek.getHead().getX()][(int) snek.getHead().getY()] = 1;
        } else if ((lastKey == Direction.UP && key == Direction.LEFT) || (lastKey == Direction.RIGHT && key == Direction.DOWN)) {
            turnArray[(int) snek.getHead().getX()][(int) snek.getHead().getY()] = 4;
        } else turnArray[(int) snek.getHead().getX()][(int) snek.getHead().getY()] = 0;
    }

    /**
     * Synchronizes the legacy game-over label with the snake's collision state.
     *
     * @param snek current snake
     */
    public void gameOver(Snake snek) {
        if (snek.getDead() && !gameOverVis) {
            gameOverVis = true;
            gameOverLabel.setVisible(true);
        } else if (!snek.getDead() && gameOverVis) {
            gameOverLabel.setVisible(false);
            gameOverVis = false;
        }
    }

    /**
     * Synchronizes the legacy victory label with the game's win condition.
     */
    public void gameWon() {
        if (game.gameWon() && !gameWonVis) {
            gameWonVis = true;
            gameWonLabel.setVisible(true);
        } else if (!game.gameWon() && gameWonVis) {
            gameWonVis = false;
            gameWonLabel.setVisible(false);
        }
    }

    /**
     * Updates the level objective and currently active combo labels.
     *
     * @param session session supplying the game rules and current state
     */
    public void updateProgress(GameSession session) {
        if (session.getLevel() != null) {
            progressLabel.setText(session.getLevel() + "  |  Coins: " + session.getLevelCoins()
                    + "/" + session.getLevel().target());
            int count = session.getComboCount();
            comboLabel.setText(count == 0 ? "" : "Combo x" + count + " (+" + session.getComboBonus() + ")");
        }
    }

    /**
     * Displays the current accumulated run score.
     */
    public void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getScorePoints());
    }

}
