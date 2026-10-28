package com.comp2042;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Checks the snake's legal direction changes without starting a JavaFX window.
 */
class GameControllerTest {

    @ParameterizedTest(name = "{0} to {1}: allowed={2}")
    @CsvSource({
            "UP, UP, true",
            "UP, DOWN, false",
            "UP, LEFT, true",
            "UP, RIGHT, true",
            "DOWN, UP, false",
            "DOWN, DOWN, true",
            "DOWN, LEFT, true",
            "DOWN, RIGHT, true",
            "LEFT, UP, true",
            "LEFT, DOWN, true",
            "LEFT, LEFT, true",
            "LEFT, RIGHT, false",
            "RIGHT, UP, true",
            "RIGHT, DOWN, true",
            "RIGHT, LEFT, false",
            "RIGHT, RIGHT, true"
    })
    void validatesDirectionChanges(Direction currentDirection, Direction requestedDirection,
                                  boolean expectedAllowed) {
        assertEquals(expectedAllowed,
                !currentDirection.isOpposite(requestedDirection));
    }

    @ParameterizedTest(name = "{0} is not a movement direction")
    @EnumSource(value = KeyCode.class,
            names = {"P", "TAB", "ESCAPE", "PLUS", "MINUS", "SPACE", "A"})
    void rejectsNonArrowKeys(KeyCode requestedDirection) {
        assertNull(GameController.directionForKey(requestedDirection));
    }
}
