package com.comp2042.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines coin objectives and fixed obstacle layouts for the standard twenty-cell square board.
 */
public enum Level {
    /** First level, requiring five coins. */
    ONE(1, 5),
    /** Second level, requiring eight coins. */
    TWO(2, 8),
    /** Final level, requiring ten coins. */
    THREE(3, 10);
    private final int number;
    private final int target;
    /**
     * Defines a level number and its collected-coin target.
     *
     * @param number one-based level number
     * @param target required number of collected coins
     */
    Level(int number, int target) { this.number = number; this.target = target; }
    /**
     * Returns the level number shown to the player.
     *
     * @return the one-based level number
     */
    public int number() { return number; }
    /**
     * Returns the objective used to complete the level.
     *
     * @return the number of coins required
     */
    public int target() { return target; }
    /**
     * Finds the next level in the run.
     *
     * @return the next level, or null after level three
     */
    public Level next() { return this == THREE ? null : values()[ordinal() + 1]; }

    /**
     * Builds a fresh copy of this level's fixed layout.
     *
     * @return obstacle coordinates on a twenty by twenty board
     */
    List<Point> obstaclePositions() {
        List<Point> points = new ArrayList<>();
        if (this == TWO) {
            for (int y = 5; y <= 8; y++) points.add(new Point(5, y));
            for (int y = 11; y <= 14; y++) points.add(new Point(14, y));
        } else if (this == THREE) {
            for (int x : new int[]{5, 14}) {
                for (int y = 4; y <= 7; y++) points.add(new Point(x, y));
                for (int y = 12; y <= 15; y++) points.add(new Point(x, y));
            }
        }
        return points;
    }
    /**
     * Formats the level display name.
     *
     * @return the level display name
     */
    @Override public String toString() { return "Level " + number; }
}
