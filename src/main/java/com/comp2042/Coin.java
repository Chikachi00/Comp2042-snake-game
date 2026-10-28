package com.comp2042;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class Coin {
    private final Point pos;

    public Coin(int x, int y, Snake snek, Obstacle obstacle) {
        this(findFreePosition(x, y, snek.getHead(), snek.getBody(),
                obstacle.getObstacles(), new Random())
                .orElseThrow(() -> new IllegalStateException("No free cell for a coin")));
    }

    Coin(Point position) {
        pos = new Point(position);
    }

    /**
     * Selects an unoccupied board cell, or returns empty when the board is full.
     */
    static Optional<Point> findFreePosition(int width, int height, Point head,
                                            Collection<Point> body, Collection<Point> obstacles,
                                            Random random) {
        Set<Point> occupied = new HashSet<>(body);
        occupied.add(head);
        occupied.addAll(obstacles);

        List<Point> freeCells = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Point position = new Point(x, y);
                if (!occupied.contains(position)) {
                    freeCells.add(position);
                }
            }
        }
        if (freeCells.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(freeCells.get(random.nextInt(freeCells.size())));
    }

    public Point getPos() {
        return new Point(pos);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    /**
     * Identifies the coin's game category for selection and display.
     *
     * @return the category of this coin
     */
    public CoinType getType() {
        return CoinType.NORMAL;
    }
}
