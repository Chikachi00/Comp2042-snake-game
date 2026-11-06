package com.comp2042.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Represents an ordinary five-point coin and the shared placement and effect contract.
 */
public class Coin {
    private final Point pos;

    /**
     * Places an ordinary coin in a randomly selected free cell; a full board cannot accept a coin.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param snek current snake
     * @param obstacle board obstacle collection
     */
    public Coin(int x, int y, Snake snek, Obstacle obstacle) {
        this(findFreePosition(x, y, snek.getHead(), snek.getBody(),
                obstacle.getObstacles(), new Random())
                .orElseThrow(() -> new IllegalStateException("No free cell for a coin")));
    }

    /**
     * Stores an independent copy of the supplied coin position.
     *
     * @param position board position in cell coordinates
     */
    Coin(Point position) {
        pos = new Point(position);
    }

    /**
     * Selects a uniformly random free cell without retry loops.
     *
     * @param width board width in cells
     * @param height board height in cells
     * @param head occupied head position
     * @param body tail-to-head body coordinates
     * @param obstacles occupied obstacle coordinates
     * @param random random source for selection or effects
     * @return a free position, or an empty optional if the board is full
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

    /**
     * Returns an independent copy of the coin coordinate.
     *
     * @return the current coin position
     */
    public Point getPos() {
        return new Point(pos);
    }

    /**
     * Returns the base value before combo bonuses.
     *
     * @return five points
     */
    public int getPoints() {
        int points = 5;
        return points;
    }

    /**
     * Applies the ordinary coin effect, which uses the configured base speed.
     *
     * @param game game settings and score state
     * @param snake current snake
     * @param obstacle board obstacle collection
     * @param random random source for selection or effects
     * @return the number of moves per second after the effect
     */
    double applyEffect(Game game, Snake snake, Obstacle obstacle, Random random) {
        return game.getSpeed();
    }

    /**
     * Identifies the category used for selection and sprite lookup.
     *
     * @return the NORMAL coin category
     */
    public CoinType getType() {
        return CoinType.NORMAL;
    }
}
