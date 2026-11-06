package com.comp2042.game;

import java.awt.Point;
import java.util.Random;

/**
 * Allows border wrapping until another coin replaces its temporary effect.
 */
public class BlueCoin extends Coin {
    /**
     * Places this special coin in a randomly selected free cell.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param snek current snake
     * @param obstacle board obstacle collection
     */
    public BlueCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    /**
     * Stores an independent copy of the supplied special-coin position.
     *
     * @param position board position in cell coordinates
     */
    BlueCoin(Point position) {
        super(position);
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
     * Enables temporary border wrapping and restores the base speed.
     *
     * @param game game settings and score state
     * @param snake current snake
     * @param obstacle board obstacle collection
     * @param random random source for selection or effects
     * @return the number of moves per second after the effect
     */
    @Override
    double applyEffect(Game game, Snake snake, Obstacle obstacle, Random random) {
        snake.ignoreBorders(true);
        return game.getSpeed();
    }

    /**
     * Identifies the category used for selection and sprite lookup.
     *
     * @return the BLUE coin category
     */
    @Override
    public CoinType getType() {
        return CoinType.BLUE;
    }
}
