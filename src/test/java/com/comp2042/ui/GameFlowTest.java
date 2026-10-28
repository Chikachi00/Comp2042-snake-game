package com.comp2042.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameFlowTest {
    @Test void pauseBeforeStartingDoesNotStartOnResume() {
        GameFlow flow = new GameFlow();
        assertTrue(flow.pause());
        assertTrue(flow.isPaused());
        assertFalse(flow.resume());
        assertFalse(flow.isPaused());
    }
    @Test void pausedRunningGameResumesAndCannotPauseTwice() {
        GameFlow flow = new GameFlow();
        flow.start();
        assertTrue(flow.pause());
        assertFalse(flow.pause());
        assertTrue(flow.resume());
        assertFalse(flow.isPaused());
    }
    @Test void endedGameCannotPauseAndResetRestoresReadyState() {
        GameFlow flow = new GameFlow();
        flow.start();
        flow.end();
        assertFalse(flow.pause());
        flow.reset();
        assertTrue(flow.pause());
        assertFalse(flow.resume());
    }
}
