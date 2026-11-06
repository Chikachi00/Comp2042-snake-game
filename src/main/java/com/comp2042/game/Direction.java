package com.comp2042.game;

/**
 * Represents movement offsets and opposite-direction checks in board coordinates.
 */
public enum Direction {
    /** Moves one row upward. */
    UP(0, -1),
    /** Moves one row downward. */
    DOWN(0, 1),
    /** Moves one column left. */
    LEFT(-1, 0),
    /** Moves one column right. */
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    /**
     * Defines the signed movement offset for a direction.
     *
     * @param dx horizontal offset in cells
     * @param dy vertical offset in cells
     */
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Returns the horizontal movement offset.
     *
     * @return minus one, zero or one column
     */
    int dx() { return dx; }
    /**
     * Returns the vertical movement offset.
     *
     * @return minus one, zero or one row
     */
    int dy() { return dy; }

    /**
     * Tests whether another direction would reverse the movement.
     *
     * @param other direction to compare, possibly null
     * @return whether the other direction is opposite; false for null
     */
    public boolean isOpposite(Direction other) {
        return other != null && dx == -other.dx && dy == -other.dy;
    }
}
