package com.comp2042;

import java.awt.Point;

public class SilverCoin extends Coin {
    public SilverCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    SilverCoin(Point position) {
        super(position);
    }

    @Override
    public CoinType getType() {
        return CoinType.SILVER;
    }

    public int getPoints() {
        int points = 5;
        return points;
    }
}
