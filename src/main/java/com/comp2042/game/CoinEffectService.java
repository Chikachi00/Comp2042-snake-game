package com.comp2042.game;

import java.util.Random;

/**
 * Replaces temporary coin effects and awards the coin's base score.
 */
final class CoinEffectService {
    private final Random random;

    /**
     * Creates a helper with an independent default random source.
     */
    CoinEffectService() {
        this(new Random());
    }

    /**
     * Creates a helper with the supplied random source.
     *
     * @param random random source for selection or effects
     */
    CoinEffectService(Random random) {
        this.random = random;
    }

    /**
     * Clears temporary border wrapping, applies the new coin effect and awards its base points.
     *
     * @param coin current or collected coin
     * @param game game settings and score state
     * @param snake current snake
     * @param obstacle board obstacle collection
     * @return the speed to apply to the JavaFX timer
     */
    double apply(Coin coin, Game game, Snake snake, Obstacle obstacle) {
        snake.ignoreBorders(false);
        double speed = coin.applyEffect(game, snake, obstacle, random);
        game.addPoints(coin.getPoints());
        return speed;
    }
}
