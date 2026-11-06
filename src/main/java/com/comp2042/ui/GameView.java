package com.comp2042.ui;

import com.comp2042.game.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import java.awt.Point;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Draws the square board and presents HUD, optional shortcuts, pause and end menus. */
public class GameView {
    private Canvas gridCanvas;
    private GraphicsContext gc;
    private Label gameOverLabel, gameWonLabel, scoreLabel, progressLabel, comboLabel, startHint;
    private Label comboTime, shortcutStatus, noticeLabel, ruleHint, menuTitle, menuDetails;
    private ProgressBar comboBar;
    private VBox menu;
    private Button resumeButton, restartButton, mainMenuButton;
    private StackPane boardHost, boardLayer;
    private VBox hud, footer;
    private BorderPane frame;
    private boolean gameOverVis, gameWonVis, fullsize;
    private double initX, initY;
    private double fullScale = 1;
    private Coin lastCoin;
    private Snake lastSnake;
    private Obstacle lastObstacle;
    private List<ShortcutSnapshot> shortcuts = List.of();
    private final Game game;
    private final int[][] turnArray;
    private final ImageView imageHead = new ImageView(new Image("resources/views/res/snakehead.png"));
    private final ImageView imageBody = new ImageView(new Image("resources/views/res/snakebody.png"));
    private final ImageView imageTurn = new ImageView(new Image("resources/views/res/snaketurnleft.png"));
    private final ImageView imageTail = new ImageView(new Image("resources/views/res/snaketail.png"));
    private final Map<CoinType, Image> coinImages = Map.of(
            CoinType.NORMAL, new Image("resources/views/res/redcoin.png"),
            CoinType.GOLD, new Image("resources/views/res/goldcoin.png"),
            CoinType.RAINBOW, new Image("resources/views/res/rainbowcoin.png"),
            CoinType.BLUE, new Image("resources/views/res/bluecoin.png"),
            CoinType.SILVER, new Image("resources/views/res/silvercoin.png"));

    /** @param game board settings and score state */
    public GameView(Game game) {
        this.game = game;
        turnArray = new int[game.getWidth()][game.getHeight()];
    }

    /**
     * Creates a fixed base-size board independently of the surrounding window and HUD.
     * @param scene scene hosting the game
     * @param snek current snake
     * @param coin current coin
     */
    public void makeScene(Scene scene, Snake snek, Coin coin) {
        initX = game.getWidth() * game.getMultiplier();
        initY = game.getHeight() * game.getMultiplier();
        gridCanvas = new Canvas(initX, initY);
        gridCanvas.setId("gridCanvas");
        gc = gridCanvas.getGraphicsContext2D();
        gameOverLabel = label("gameOverLabel", "GAME OVER", 30);
        gameWonLabel = label("gameWonLabel", "YOU WON!", 30);
        gameOverLabel.setVisible(false); gameWonLabel.setVisible(false);
        scoreLabel = label("scoreLabel", "Score: " + game.getScorePoints(), 20);
        progressLabel = label("progressLabel", "", 14);
        comboLabel = label("comboLabel", "", 12);
        comboTime = label("comboTime", "", 12);
        comboTime.setMinWidth(40);
        comboBar = new ProgressBar(0);
        comboBar.setId("comboBar");
        comboBar.setPrefWidth(96); comboBar.setMinHeight(8); comboBar.setPrefHeight(8); comboBar.setMaxHeight(8);
        comboBar.getStyleClass().add("combo-timer");
        HBox firstRow = new HBox(8, progressLabel, spacer(), scoreLabel);
        HBox secondRow = new HBox(6, comboLabel, spacer(), comboBar, comboTime);
        firstRow.setAlignment(Pos.CENTER_LEFT); secondRow.setAlignment(Pos.CENTER_LEFT);
        hud = new VBox(3, firstRow, secondRow);
        hud.setAlignment(Pos.CENTER); hud.setMaxWidth(initX);
        StackPane header = new StackPane(hud);
        header.setId("gameHud"); header.setPrefHeight(48); header.setMinHeight(48); header.setMaxHeight(48);

        shortcutStatus = label("shortcutStatus", "", 11);
        noticeLabel = label("shortcutNotice", "", 11);
        noticeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #476b35;");
        HBox statusRow = new HBox(6, shortcutStatus, spacer(), noticeLabel);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        startHint = label("startHint", "Press an arrow key to start", 11);
        ruleHint = label("ruleHint", "", 11);
        StackPane hints = new StackPane(ruleHint, startHint);
        hints.setAlignment(Pos.CENTER_LEFT);
        footer = new VBox(2, statusRow, hints);
        footer.setAlignment(Pos.CENTER); footer.setMaxWidth(initX);
        StackPane footerHost = new StackPane(footer);
        footerHost.setId("gameFooter"); footerHost.setPrefHeight(32); footerHost.setMinHeight(32); footerHost.setMaxHeight(32);
        ruleHint.setVisible(false);

        menuTitle = label("menuTitle", "", 25);
        menuDetails = label("menuDetails", "", 13);
        menuTitle.setStyle("-fx-font-size: 25; -fx-text-fill: white;");
        menuDetails.setStyle("-fx-font-size: 13; -fx-text-fill: white;");
        resumeButton = button("resumeButton", "Resume");
        restartButton = button("restartButton", "Restart");
        mainMenuButton = button("mainMenuButton", "Main Menu");
        menu = new VBox(12, menuTitle, menuDetails, resumeButton, restartButton, mainMenuButton);
        menu.setId("gameMenu"); menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: rgba(20,24,30,0.9); -fx-padding: 18;");
        hideMenu();
        boardLayer = new StackPane(gridCanvas, gameOverLabel, gameWonLabel, menu);
        boardLayer.setMinSize(0, 0); boardLayer.setPrefSize(initX, initY);
        boardLayer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        boardHost = new StackPane(boardLayer);
        boardHost.setId("boardHost"); boardHost.setMinSize(0, 0);
        boardHost.widthProperty().addListener((o, a, b) -> resizeToAvailableSpace());
        boardHost.heightProperty().addListener((o, a, b) -> resizeToAvailableSpace());
        frame = new BorderPane(boardHost, header, null, footerHost, null);
        frame.setId("gameFrame"); frame.setStyle("-fx-background-color: #f1f2e4;");
        frame.getStylesheets().add("resources/views/styling.css");
        drawGrid(coin, snek, null);
    }

    /**
     * Creates a label with the bundled font and explicit HUD size.
     * @param id lookup identifier
     * @param text visible text
     * @param size font size
     * @return a pixel-font label
     */
    private static Label label(String id, String text, double size) {
        Label label = new Label(text);
        label.setId(id); label.setFont(Font.font("SF Pixelate", size));
        label.setStyle("-fx-font-size: " + (int) size + ";");
        return label;
    }

    /** @return flexible space between HUD fields */
    private static Region spacer() { Region space = new Region(); HBox.setHgrow(space, Priority.ALWAYS); return space; }

    /**
     * Creates a menu button that fits its full text inside the original button image.
     * @param id lookup identifier
     * @param text button text
     * @return a menu button with original styling
     */
    private static Button button(String id, String text) {
        Button button = new Button(text); button.setId(id); button.setPrefSize(115, 40);
        button.setMinSize(115, 40); button.setMaxSize(115, 40);
        button.setStyle("-fx-font-size: 14;"); return button;
    }

    /**
     * Connects menu actions to the controller.
     * @param resume resume action
     * @param restart restart action
     * @param mainMenu navigation action
     */
    public void setMenuActions(Runnable resume, Runnable restart, Runnable mainMenu) {
        resumeButton.setOnAction(e -> resume.run()); restartButton.setOnAction(e -> restart.run());
        mainMenuButton.setOnAction(e -> mainMenu.run());
    }

    /** Shows all pause actions without changing the board or HUD allocation. */
    public void showPauseMenu() {
        menuTitle.setText("Paused"); menuDetails.setText("P / Esc: Resume    Tab: Restart");
        resumeButton.setVisible(true); resumeButton.setManaged(true); menu.setManaged(true); menu.setVisible(true);
    }

    /** @param won whether the run ended in victory */
    public void showEndMenu(boolean won) {
        gameOverLabel.setVisible(false); gameWonLabel.setVisible(false);
        menuTitle.setText(won ? "You won!" : "Game over");
        menuDetails.setText("Final score: " + game.getScorePoints());
        resumeButton.setVisible(false); resumeButton.setManaged(false); menu.setManaged(true); menu.setVisible(true);
    }

    /** Hides the pause/end overlay. */
    public void hideMenu() { menu.setVisible(false); menu.setManaged(false); }
    /** Shows the arrow-key hint in the footer, outside the board. */
    public void showStartHint() { startHint.setVisible(true); ruleHint.setVisible(false); }
    /** Replaces the start hint with the current rule reminder. */
    public void hideStartHint() { startHint.setVisible(false); ruleHint.setVisible(true); }
    /** Clears corner state after a restart or level change. */
    public void resetTurns() { for (int[] column : turnArray) java.util.Arrays.fill(column, 0); }

    /** @param gamePane scene root receiving the board and the separate HUD/footer */
    public void attachTo(StackPane gamePane) {
        gamePane.getChildren().add(frame);
    }

    /**
     * Synchronizes current model snapshots before redrawing, including newly opened gates.
     * @param session current session
     */
    public void render(GameSession session) {
        updateProgress(session); updateScoreLabel(); drawGrid(session.getCoin(), session.getSnake(), session.getObstacle());
    }

    /**
     * Matches the board to the actual fullscreen state; every tile uses one scale factor.
     * @param fullScreen actual window fullscreen state
     * @param obstacle current obstacles
     * @param snek current snake
     * @param coin current coin
     */
    public void resizeForFullscreen(boolean fullScreen, Obstacle obstacle, Snake snek, Coin coin) {
        fullsize = fullScreen;
        if (fullScreen) {
            double side = Math.min(Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight() - 80);
            applyScale(Math.max(1, side / initY));
            resizeToAvailableSpace();
        } else applyScale(1);
        drawGrid(coin, snek, obstacle);
    }

    /** Fits fullscreen drawing inside the allocated board container, excluding HUD and footer. */
    private void resizeToAvailableSpace() {
        if (!fullsize || boardHost == null || boardHost.getScene() == null
                || boardHost.getScene().getHeight() <= 480) return;
        double scale = Math.min(boardHost.getWidth() / initX, boardHost.getHeight() / initY);
        if (scale > 0 && Math.abs(scale - fullScale) > 0.0001) {
            applyScale(scale);
            if (lastCoin != null) drawGrid(lastCoin, lastSnake, lastObstacle);
        }
    }

    /** @param scale absolute ratio to the base board dimensions */
    private void applyScale(double scale) {
        fullScale = scale; gridCanvas.setWidth(initX * scale); gridCanvas.setHeight(initY * scale);
        if (boardLayer != null) boardLayer.setPrefSize(gridCanvas.getWidth(), gridCanvas.getHeight());
        if (hud != null) hud.setMaxWidth(gridCanvas.getWidth());
        if (footer != null) footer.setMaxWidth(gridCanvas.getWidth());
    }

    /**
     * Redraws all board objects and remembers the current models for fullscreen layout changes.
     * @param coin coin to draw
     * @param snek snake to draw
     * @param obstacle walls to draw, or null during setup
     */
    public void drawGrid(Coin coin, Snake snek, Obstacle obstacle) {
        lastCoin = coin; lastSnake = snek; lastObstacle = obstacle; drawBoardContents(coin, snek);
    }

    /** Draws paired pixel keys and numbered gates behind the snake and coin. */
    private void drawShortcuts() {
        double cell = game.getMultiplier() * fullScale;
        gc.save(); gc.setTextAlign(TextAlignment.CENTER);
        for (ShortcutSnapshot s : shortcuts) {
            Color color = Color.web(s.id().equals("A") ? "#a9751b" : "#376e94");
            double x = s.gateX() * cell, y = s.gateY() * cell;
            gc.setFill(color);
            if (s.open()) {
                gc.fillRect(x, y, cell * .10, cell); gc.fillRect(x + cell * .90, y, cell * .10, cell);
            } else {
                gc.fillRect(x, y, cell, cell);
                gc.setFill(Color.web("#e3d8b9"));
                gc.fillRect(x + cell*.14, y + cell*.12, cell*.72, cell*.09);
                gc.setFill(Color.WHITE); gc.setFont(Font.font("SF Pixelate", cell * .55));
                gc.fillText(s.id(), x + cell*.5, y + cell*.81);
            }
            if (s.keyAvailable()) {
                x = s.keyX() * cell; y = s.keyY() * cell;
                gc.setStroke(color); gc.setLineWidth(Math.max(1, cell*.1));
                gc.strokeOval(x + cell*.14, y + cell*.13, cell*.32, cell*.32);
                gc.setFill(color); gc.fillRect(x + cell*.4, y + cell*.29, cell*.45, cell*.10);
                gc.fillRect(x + cell*.65, y + cell*.32, cell*.10, cell*.18);
                gc.setFont(Font.font("SF Pixelate", cell*.45));
                gc.fillText(s.id(), x + cell*.65, y + cell*.94);
            }
        }
        gc.restore();
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
        if (lastObstacle != null) {
            gc.setFill(lastObstacle.getColor());
            double cell = game.getMultiplier() * fullScale;
            for (Point p : lastObstacle.getObstacles()) gc.fillRect(p.x * cell, p.y * cell, cell, cell);
        }
        drawShortcuts();

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
     * Updates shortcut snapshots, rule reminders and the shared-clock combo countdown.
     * @param session current session
     */
    public void updateProgress(GameSession session) {
        shortcuts = session.getShortcuts();
        if (session.getLevel() != null) {
            progressLabel.setText(session.getLevel() + "  |  Coins: " + session.getLevelCoins()
                    + "/" + session.getLevel().target());
        } else {
            progressLabel.setText(session.getMode().toString());
        }
        int count = session.getComboCount();
        comboLabel.setText(session.getMode() == GameMode.ENDLESS ? "Classic survival"
                : count == 0 ? "" : "Combo x" + count + " (+" + session.getComboBonus() + ")");
        double remaining = session.getComboSecondsRemaining();
        comboBar.setProgress(remaining / 5.0); comboBar.setVisible(count > 0);
        comboTime.setText(count == 0 ? "" : String.format(Locale.ROOT, "%.1fs", remaining));
        shortcutStatus.setText(shortcuts.stream().map(s -> s.id() + ": "
                + (s.open() ? "Open" : s.keyAvailable() ? "Key ready" : "Locked")).collect(Collectors.joining("  |  ")));
        noticeLabel.setText(session.getShortcutMessage());
        if (session.getMode() == GameMode.ENDLESS) ruleHint.setText("Collect coins. Avoid walls and your own tail.");
        else if (shortcuts.isEmpty()) ruleHint.setText("Eat coins within 5 seconds to keep your combo.");
        else if (session.getLevelCoins() == 0) ruleHint.setText("Collect 1 coin to reveal keys. Gates are optional.");
        else if (shortcuts.stream().allMatch(ShortcutSnapshot::open)) ruleHint.setText("Shortcuts open. Keep your combo going.");
        else ruleHint.setText("Match keys to gates. You can always go around.");
    }

    /** Displays accumulated run points. */
    public void updateScoreLabel() { scoreLabel.setText("Score: " + game.getScorePoints()); }
}
