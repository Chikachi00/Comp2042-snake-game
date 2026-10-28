package com.comp2042;

import java.awt.Point;

public class RainbowCoin extends Coin {
    public RainbowCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    RainbowCoin(Point position) {
        super(position);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    public double getSpeedBonus() {
        double speedBonus = 5;
        return speedBonus;
    }

    @Override
    public CoinType getType() {
        return CoinType.RAINBOW;
    }
}
