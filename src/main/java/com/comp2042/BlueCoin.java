package com.comp2042;

import java.awt.Point;

public class BlueCoin extends Coin {
    public BlueCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    BlueCoin(Point position) {
        super(position);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    @Override
    public CoinType getType() {
        return CoinType.BLUE;
    }
}
