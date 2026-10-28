package com.comp2042.ui;

import com.comp2042.game.Coin;
import com.comp2042.game.CoinType;
import com.comp2042.game.Direction;
import com.comp2042.game.Game;
import com.comp2042.game.Obstacle;
import com.comp2042.game.Snake;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.util.Map;
import java.util.List;

/** Renders the game board and owns its sprite images. */
public class GameView {

    private Canvas gridCanvas;
    private GraphicsContext gc;

    private Label gameOverLabel;
    private Label gameWonLabel;
    private Label scoreLabel;
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

    public GameView(Game game) {
        this.game = game;
        this.turnArray = new int[game.getWidth()][game.getHeight()];
    }

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
    }

    /** Adds the board and its labels to the game pane in display order. */
    public void attachTo(StackPane gamePane) {
        gamePane.getChildren().addAll(gridCanvas, gameOverLabel, gameWonLabel, scoreLabel);
        gamePane.setPadding(new Insets(20));
        StackPane.setAlignment(gameOverLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(gameWonLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(scoreLabel, Pos.BOTTOM_CENTER);
    }

    public void scaleToFullscreen(boolean fullScreen, Stage primaryStage, Obstacle obstacle, Snake snek, Coin coin) {
        primaryStage.setFullScreen(fullScreen);

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

    /** Redraws the current game state, including any obstacles. */
    public void drawGrid(Coin coin, Snake snek, Obstacle obstacle) {
        drawBoardContents(coin, snek);
        gc.setFill(obstacle.getColor());
        for (Point point : obstacle.getObstacles()) {
            gc.fillRect(point.x * game.getMultiplier() * fullScale, point.y * game.getMultiplier() * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
        }
    }

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

    private static int rotation(Direction direction) {
        return switch (direction) {
            case UP -> 0;
            case RIGHT -> 90;
            case DOWN -> 180;
            case LEFT -> 270;
        };
    }

    private void drawHead(Snake snek) {
        imageHead.setRotate(rotation(snek.getDirection()));
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image rotatedImage = imageHead.snapshot(params, null);
        gc.drawImage(rotatedImage, (snek.getHead().x * game.getMultiplier()) * fullScale, (snek.getHead().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale * fullScale, game.getMultiplier() * fullScale * fullScale);
    }

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

        gc.drawImage(rotatedImage, (point.x * game.getMultiplier()) * fullScale, (point.y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale * fullScale, game.getMultiplier() * fullScale * fullScale);
    }

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

    public void gameOver(Snake snek) {
        if (snek.getDead() && !gameOverVis) {
            gameOverVis = true;
            gameOverLabel.setVisible(true);
        } else if (!snek.getDead() && gameOverVis) {
            gameOverLabel.setVisible(false);
            gameOverVis = false;
        }
    }

    public void gameWon() {
        if (game.gameWon() && !gameWonVis) {
            gameWonVis = true;
            gameWonLabel.setVisible(true);
        } else if (!game.gameWon() && gameWonVis) {
            gameWonVis = false;
            gameWonLabel.setVisible(false);
        }
    }

    public void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getScorePoints());
    }

}
