package com.comp2042.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoinTest {
    @ParameterizedTest
    @CsvSource({"NORMAL, 5", "GOLD, 15", "RAINBOW, 5", "BLUE, 5", "SILVER, 5"})
    void createsEachCoinWithoutStartingJavaFx(String kind, int expectedPoints) {
        Point position = new Point(1, 2);
        Coin coin = switch (kind) {
            case "NORMAL" -> new Coin(position);
            case "GOLD" -> new GoldCoin(position);
            case "RAINBOW" -> new RainbowCoin(position);
            case "BLUE" -> new BlueCoin(position);
            case "SILVER" -> new SilverCoin(position);
            default -> throw new IllegalArgumentException(kind);
        };

        assertEquals(position, coin.getPos());
        assertEquals(expectedPoints, coin.getPoints());
        assertEquals(CoinType.valueOf(kind), coin.getType());
    }

    @Test
    void doesNotExposeTheMutablePosition() {
        Point input = new Point(1, 2);
        Coin coin = new Coin(input);
        input.setLocation(8, 8);
        coin.getPos().setLocation(9, 9);

        assertEquals(new Point(1, 2), coin.getPos());
    }

    @Test
    void rainbowCoinRetainsItsSpeedBonus() {
        assertEquals(5.0, new RainbowCoin(new Point(1, 2)).getSpeedBonus());
    }
}