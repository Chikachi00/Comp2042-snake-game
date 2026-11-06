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
 * Stores obstacle coordinates and selects free cells for bounded, non-overlapping layouts.
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

    /**
     * Creates an empty obstacle collection for the given board.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param random random source for selection or effects
     */
    Obstacle(int x, int y, Random random) {
        this.x = x;
        this.y = y;
        this.random = random;
    }

    /**
     * Adds distinct free cells, excluding the snake, coin and existing obstacles; stops at the space
     * or fifty-obstacle limit.
     *
     * @param snake current snake
     * @param coin current or collected coin
     * @param amount requested number of additional obstacles
     */
    public void newObstacles(Snake snake, Coin coin, int amount) {
        newObstacles(snake.getHead(), snake.getBody(), coin.getPos(), amount);
    }

    /**
     * Adds distinct free cells, excluding the snake, coin and existing obstacles; stops at the space
     * or fifty-obstacle limit.
     *
     * @param snakeHead occupied head position
     * @param snakeBody occupied body positions
     * @param coinPosition position of the current coin
     * @param amount requested number of additional obstacles
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

    /**
     * Returns the obstacle drawing color.
     *
     * @return the JavaFX obstacle color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns an unmodifiable snapshot with independent coordinate objects.
     *
     * @return the occupied obstacle cells
     */
    public List<Point> getObstacles() {
        return obstacles.stream().map(Point::new).toList();
    }

    /**
     * Checks whether an obstacle occupies a coordinate.
     *
     * @param position board position in cell coordinates
     * @return whether the position is blocked
     */
    boolean occupies(Point position) {
        return obstacles.contains(position);
    }

    /**
     * Replaces the layout after checking bounds, uniqueness and the obstacle limit.
     *
     * @param positions distinct in-bounds obstacle coordinates, at most fifty
     */
    void setLayout(List<Point> positions) {
        Set<Point> unique = new HashSet<>();
        for (Point p : positions) {
            if (p.x < 0 || p.x >= x || p.y < 0 || p.y >= y || !unique.add(p)) {
                throw new IllegalArgumentException("Invalid obstacle position: " + p);
            }
        }
        if (unique.size() > MAX_OBSTACLES) throw new IllegalArgumentException("Too many obstacles");
        obstacles.clear();
        positions.forEach(p -> obstacles.add(new Point(p)));
    }

    /**
     * Removes all obstacles.
     */
    public void reset() {
        obstacles.clear();
    }
}
