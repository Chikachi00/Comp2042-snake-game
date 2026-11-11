package com.comp2042.game;

import java.awt.Point;
import java.util.Random;

/**
 * Adds random obstacles in free cells and adjusts the board-fill win threshold.
 */
public class SilverCoin extends Coin {
    /**
     * Places this special coin in a randomly selected free cell.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param snek current snake
     * @param obstacle board obstacle collection
     */
    public SilverCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    /**
     * Stores an independent copy of the supplied special-coin position.
     *
     * @param position board position in cell coordinates
     */
    SilverCoin(Point position) {
        super(position);
    }

    /**
     * Adds free-cell obstacles and reduces the board-fill threshold by the actual number added.
     *
     * @param game game settings and score state
     * @param snake current snake
     * @param obstacle board obstacle collection
     * @param random random source for selection or effects
     * @return the number of moves per second after the effect
     */
    @Override
    double applyEffect(Game game, Snake snake, Obstacle obstacle, Random random) {
        int previousCount = obstacle.getObstacles().size();
        int amount = random.nextInt(Math.max(1, game.getWidth() * game.getHeight() / 20)) + 3;
        obstacle.newObstacles(snake, this, amount);
        game.decMaxScore(obstacle.getObstacles().size() - previousCount);
        return game.getSpeed();
    }

    /**
     * Identifies the category used for selection and sprite lookup.
     *
     * @return the SILVER coin category
     */
    @Override
    public CoinType getType() {
        return CoinType.SILVER;
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
}
