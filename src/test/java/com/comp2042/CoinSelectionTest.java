package com.comp2042;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks every random outcome without relying on chance or opening a JavaFX window.
 */
class CoinSelectionTest {

    @ParameterizedTest(name = "roll {0}: clear board={1}, obstacles={2}")
    @CsvSource({
            "0, NORMAL, NORMAL",
            "1, GOLD, GOLD",
            "2, RAINBOW, RAINBOW",
            "3, BLUE, BLUE",
            "4, SILVER, NORMAL",
            "5, NORMAL, NORMAL",
            "6, NORMAL, NORMAL",
            "7, NORMAL, NORMAL",
            "8, NORMAL, NORMAL",
            "9, NORMAL, NORMAL",
            "10, NORMAL, NORMAL",
            "11, NORMAL, NORMAL"
    })
    void selectsExpectedCoinForEveryRoll(int roll, CoinType expectedClearBoard,
                                        CoinType expectedWithObstacles) {
        assertAll(
                () -> assertEquals(expectedClearBoard,
                        GameController.selectCoinType(roll, true, false)),
                () -> assertEquals(expectedWithObstacles,
                        GameController.selectCoinType(roll, true, true))
        );
    }

    @ParameterizedTest(name = "roll {0}: power-ups disabled")
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11})
    void selectsNormalCoinsWhenPowerUpsAreDisabled(int roll) {
        assertAll(
                () -> assertEquals(CoinType.NORMAL,
                        GameController.selectCoinType(roll, false, false)),
                () -> assertEquals(CoinType.NORMAL,
                        GameController.selectCoinType(roll, false, true))
        );
    }
}
