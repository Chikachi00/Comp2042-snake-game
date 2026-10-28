package com.comp2042;

import java.awt.Point;
import java.util.Random;

public class SilverCoin extends Coin {
    public SilverCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    SilverCoin(Point position) {
        super(position);
    }

    @Override
    double applyEffect(Game game, Snake snake, Obstacle obstacle, Random random) {
        int previousCount = obstacle.getObstacles().size();
        int amount = random.nextInt(game.getWidth() * game.getHeight() / 20) + 3;
        obstacle.newObstacles(snake, this, amount);
        game.decMaxScore(obstacle.getObstacles().size() - previousCount);
        return game.getSpeed();
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
