package com.comp2042.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Builds deterministic obstacle layouts through the production generation API. */
final class ObstacleFixtures {
    private ObstacleFixtures() { }

    static void place(Obstacle obstacle, int width, int height, Point... positions) {
        Set<Point> target = new HashSet<>(Arrays.asList(positions));
        List<Point> excluded = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Point cell = new Point(x, y);
                if (!target.contains(cell)) excluded.add(cell);
            }
        }
        obstacle.newObstacles(new Point(-1, -1), excluded, new Point(-1, -1), target.size());
        if (!new HashSet<>(obstacle.getObstacles()).equals(target)) {
            throw new AssertionError("Could not construct the expected obstacle layout");
        }
    }
}
