package com.comp2042.game;

import java.util.Random;

/** Applies coin effects and scoring without depending on the JavaFX timer. */
final class CoinEffectService {
    private final Random random;

    CoinEffectService() {
        this(new Random());
    }

    CoinEffectService(Random random) {
        this.random = random;
    }

    /**
     * Clears the previous temporary effect, applies the coin, and awards points.
     *
     * @return the speed the game timer should use after eating the coin
     */
    double apply(Coin coin, Game game, Snake snake, Obstacle obstacle) {
        snake.ignoreBorders(false);
        double speed = coin.applyEffect(game, snake, obstacle, random);
        game.addPoints(coin.getPoints());
        return speed;
    }
}
