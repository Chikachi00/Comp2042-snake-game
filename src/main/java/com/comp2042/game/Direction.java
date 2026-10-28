package com.comp2042.game;

/** The four directions in which the snake can move on the board. */
public enum Direction {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    int dx() { return dx; }
    int dy() { return dy; }

    public boolean isOpposite(Direction other) {
        return other != null && dx == -other.dx && dy == -other.dy;
    }
}
