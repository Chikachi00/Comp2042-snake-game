package com.comp2042.game;

import java.awt.Point;
import java.util.Random;

/**
 * Adds five moves per second until the next coin or level reset.
 */
public class RainbowCoin extends Coin {
    /**
     * Places this special coin in a randomly selected free cell.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param snek current snake
     * @param obstacle board obstacle collection
     */
    public RainbowCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    /**
     * Stores an independent copy of the supplied special-coin position.
     *
     * @param position board position in cell coordinates
     */
    RainbowCoin(Point position) {
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
     * Returns the temporary speed increase.
     *
     * @return five additional moves per second
     */
    public double getSpeedBonus() {
        double speedBonus = 5;
        return speedBonus;
    }

    /**
     * Applies the temporary speed increase.
     *
     * @param game game settings and score state
     * @param snake current snake
     * @param obstacle board obstacle collection
     * @param random random source for selection or effects
     * @return the number of moves per second after the effect
     */
    @Override
    double applyEffect(Game game, Snake snake, Obstacle obstacle, Random random) {
        return game.getSpeed() + getSpeedBonus();
    }

    /**
     * Identifies the category used for selection and sprite lookup.
     *
     * @return the RAINBOW coin category
     */
    @Override
    public CoinType getType() {
        return CoinType.RAINBOW;
    }
}
