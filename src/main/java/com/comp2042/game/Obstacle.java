package com.comp2042.game;

import javafx.scene.paint.Color;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Stores obstacle positions and generates obstacles on the game board.
 */
public class Obstacle {
    private static final int MAX_OBSTACLES = 50;

    private final Color color = Color.BLACK;
    private final ArrayList<Point> obstacles = new ArrayList<>();
    private final int x;
    private final int y;
    private final Random random;

    /**
     * Creates an empty obstacle collection for the given board.
     *
     * @param x board width in cells
     * @param y board height in cells
     */
    public Obstacle(int x, int y) {
        this(x, y, new Random());
    }

    Obstacle(int x, int y, Random random) {
        this.x = x;
        this.y = y;
        this.random = random;
    }

    /**
     * Adds obstacles while avoiding the snake and current coin.
     *
     * @param snake the current snake
     * @param coin the current coin
     * @param amount the requested number of additional obstacles
     */
    public void newObstacles(Snake snake, Coin coin, int amount) {
        newObstacles(snake.getHead(), snake.getBody(), coin.getPos(), amount);
    }

    /**
     * Selects free cells without replacement, respecting space and the obstacle limit.
     *
     * @param snakeHead the occupied head position
     * @param snakeBody the occupied body positions
     * @param coinPosition the occupied coin position
     * @param amount the requested number of additional obstacles
     */
    void newObstacles(Point snakeHead, List<Point> snakeBody, Point coinPosition, int amount) {
        int remainingCapacity = MAX_OBSTACLES - obstacles.size();
        if (amount <= 0 || remainingCapacity <= 0) {
            return;
        }

        Set<Point> occupied = new HashSet<>(obstacles);
        occupied.add(snakeHead);
        occupied.addAll(snakeBody);
        occupied.add(coinPosition);

        List<Point> freeCells = new ArrayList<>();
        for (int column = 0; column < x; column++) {
            for (int row = 0; row < y; row++) {
                Point position = new Point(column, row);
                if (!occupied.contains(position)) {
                    freeCells.add(position);
                }
            }
        }

        Collections.shuffle(freeCells, random);
        int count = Math.min(amount, Math.min(remainingCapacity, freeCells.size()));
        obstacles.addAll(freeCells.subList(0, count));
    }

    public Color getColor() {
        return color;
    }

    /** Returns a snapshot whose coordinates cannot change the board. */
    public List<Point> getObstacles() {
        return obstacles.stream().map(Point::new).toList();
    }

    boolean occupies(Point position) {
        return obstacles.contains(position);
    }

    public void reset() {
        obstacles.clear();
    }
}
