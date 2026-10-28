package com.comp2042.game;

import java.awt.Point;
import java.util.Random;

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
    double applyEffect(Game game, Snake snake, Obstacle obstacle, Random random) {
        snake.ignoreBorders(true);
        return game.getSpeed();
    }

    @Override
    public CoinType getType() {
        return CoinType.BLUE;
    }
}
