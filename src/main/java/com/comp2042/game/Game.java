package com.comp2042.game;

import javafx.scene.paint.Color;

/**
 * Stores board settings, collected-coin counts, score points and the legacy win condition.
 */
public class Game {
    private Color color = Color.web("feffe4");
    private int width;
    private int height;
    private int score;
    private int maxScore;
    private boolean boardFull;
    private double multiplier;
    private int scorePoints;
    private double speed;
    private boolean powerUp;
    private boolean noBorder;

    /**
     * Creates board settings; E wraps borders, N uses walls and H also increases base speed.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param powerUp whether special coins may be generated
     * @param difficulty E for easy, N for normal or H for hard
     */
    public Game(int x, int y, boolean powerUp, char difficulty) {
        if (difficulty == 'E') {
            speed = 8.5;
            noBorder = true;
        } else if (difficulty == 'N') {
            speed = 8.5;
            noBorder = false;
        } else if (difficulty == 'H') {
            speed = 13;
            noBorder = false;
        }
        this.powerUp = powerUp;
        width = x;
        height = y;
        maxScore = x * y - 2;
        this.powerUp = powerUp;
        if (height > 50) {
            multiplier = 10;
        } else {
            multiplier = 20;
        }
    }

    /**
     * Checks the explicit win flag or collected-coin threshold.
     *
     * @return whether the current round has reached a win condition
     */
    public boolean gameWon() {
        return boardFull || score == maxScore;
    }

    /**
     * Sets the legacy win flag, also used when the final level is completed.
     */
    void markBoardFull() {
        boardFull = true;
    }

    /**
     * Counts one collected coin toward the board-fill threshold.
     */
    public void incScore() {
        score++;
    }

    /**
     * Reduces the board-fill threshold by the number of obstacles actually added.
     *
     * @param num number of newly blocked cells
     */
    public void decMaxScore(int num) {
        this.maxScore -= num;
    }

    /**
     * Returns the board width.
     *
     * @return the number of columns
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the board height.
     *
     * @return the number of rows
     */
    public int getHeight() {
        return height;
    }

    /**
     * Resets level-specific coin counts and win state while retaining run score points.
     *
     * @param fixedObstacles number of obstacles in the new level
     */
    void startLevel(int fixedObstacles) {
        score = 0;
        updateBoardCapacity(fixedObstacles);
        boardFull = false;
    }

    /**
     * Recalculates the fill threshold when a gate opens without changing earned points or coins.
     * @param blockedCells current number of wall and closed-gate cells
     */
    void updateBoardCapacity(int blockedCells) { maxScore = width * height - 2 - blockedCells; }

    /**
     * Clears run points, collected-coin count and the win flag for a restart.
     */
    public void resetScore() {
        score = 0;
        scorePoints = 0;
        maxScore = width * height - 2;
        boardFull = false;
    }

    /**
     * Returns the board background color retained by the model.
     *
     * @return the JavaFX board color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the unscaled size used to draw a board cell.
     *
     * @return the cell size in pixels
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Adds base coin points or a combo bonus to the run score.
     *
     * @param n points to add
     */
    public void addPoints(int n) {
        scorePoints += n;
    }

    /**
     * Returns the accumulated run score.
     *
     * @return base points plus combo bonuses
     */
    public int getScorePoints() {
        return scorePoints;
    }

    /**
     * Checks whether the coin factory may select special coins.
     *
     * @return whether special coins are enabled
     */
    public boolean isPowerUp() {
        return powerUp;
    }

    /**
     * Returns the configured speed before temporary coin effects.
     *
     * @return the base number of moves per second
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Checks the permanent border-wrapping setting.
     *
     * @return whether the difficulty allows border wrapping
     */
    public boolean isNoBorder() {
        return noBorder;
    }
}
