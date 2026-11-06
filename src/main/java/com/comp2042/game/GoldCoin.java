package com.comp2042.game;

import java.awt.Point;

/**
 * Awards fifteen base points without adding a temporary effect.
 */
public class GoldCoin extends Coin {
    /**
     * Places this special coin in a randomly selected free cell.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param snek current snake
     * @param obstacle board obstacle collection
     */
    public GoldCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    /**
     * Stores an independent copy of the supplied special-coin position.
     *
     * @param position board position in cell coordinates
     */
    GoldCoin(Point position) {
        super(position);
    }

    /**
     * Identifies the category used for selection and sprite lookup.
     *
     * @return the GOLD coin category
     */
    @Override
    public CoinType getType() {
        return CoinType.GOLD;
    }

    /**
     * Returns the base value before combo bonuses.
     *
     * @return fifteen points
     */
    public int getPoints() {
        int points = 15;
        return points;
    }
}
