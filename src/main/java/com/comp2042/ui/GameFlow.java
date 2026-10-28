package com.comp2042.ui;

/** Tracks whether a round is ready, running, paused or finished. */
final class GameFlow {
    private enum State { READY, RUNNING, PAUSED_READY, PAUSED_RUNNING, ENDED }
    private State state = State.READY;

    void start() { if (state == State.READY) state = State.RUNNING; }
    boolean pause() {
        if (state == State.READY) state = State.PAUSED_READY;
        else if (state == State.RUNNING) state = State.PAUSED_RUNNING;
        else return false;
        return true;
    }
    boolean resume() {
        if (state == State.PAUSED_RUNNING) { state = State.RUNNING; return true; }
        if (state == State.PAUSED_READY) state = State.READY;
        return false;
    }
    boolean isPaused() { return state == State.PAUSED_READY || state == State.PAUSED_RUNNING; }
    void end() { state = State.ENDED; }
    void reset() { state = State.READY; }
}
