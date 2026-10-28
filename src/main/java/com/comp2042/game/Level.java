package com.comp2042.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/** The objective and fixed obstacle layout for each playable level. */
public enum Level {
    ONE(1, 5), TWO(2, 8), THREE(3, 10);
    private final int number;
    private final int target;
    Level(int number, int target) { this.number = number; this.target = target; }
    public int number() { return number; }
    public int target() { return target; }
    public Level next() { return this == THREE ? null : values()[ordinal() + 1]; }

    /** Layouts are designed for the 20 by 20 game board. */
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
    @Override public String toString() { return "Level " + number; }
}
