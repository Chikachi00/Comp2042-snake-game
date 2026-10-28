package com.comp2042.game;

import java.awt.Point;

public class GoldCoin extends Coin {
    public GoldCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    GoldCoin(Point position) {
        super(position);
    }

    @Override
    public CoinType getType() {
        return CoinType.GOLD;
    }

    public int getPoints() {
        int points = 15;
        return points;
    }
}
