package com.comp2042;

import java.awt.Point;
import java.util.Random;

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
    double applyEffect(Game game, Snake snake, Obstacle obstacle, Random random) {
        return game.getSpeed() + getSpeedBonus();
    }

    @Override
    public CoinType getType() {
        return CoinType.RAINBOW;
    }
}
