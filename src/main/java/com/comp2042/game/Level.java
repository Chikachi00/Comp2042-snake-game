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
        if (this != ONE) {
            for (int x = 3; x <= 16; x++) if (x != 10) points.add(new Point(x, 6));
        }
        if (this == THREE) {
            for (int x = 3; x <= 16; x++) if (x != 10) points.add(new Point(x, 13));
        }
        return points;
    }

    /** @return new, independent shortcut states for a fresh attempt at this level */
    List<Shortcut> createShortcuts() {
        return switch (this) {
            case ONE -> List.of();
            case TWO -> List.of(new Shortcut("A", new Point(15, 14), new Point(10, 6)));
            case THREE -> List.of(new Shortcut("A", new Point(15, 9), new Point(10, 6)),
                    new Shortcut("B", new Point(4, 11), new Point(10, 13)));
        };
    }

    /**
     * Suggests opening coin positions to demonstrate routes around the barriers.
     * @param index zero-based coin index in this level
     * @return a preferred cell, or null to choose a random legal cell
     */
    Point preferredCoinPosition(int index) {
        if (this != ONE && index == 0) return new Point(10, 3);
        if (this == THREE && index == 1) return new Point(10, 16);
        return null;
    }
    /**
     * Formats the level display name.
     *
     * @return the level display name
     */
    @Override public String toString() { return "Level " + number; }
}
