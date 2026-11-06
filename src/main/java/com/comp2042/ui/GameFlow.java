package com.comp2042.ui;

/**
 * Tracks ready, running, paused and ended UI states, including pauses before the first move.
 */
final class GameFlow {
    /** Internal states retain whether a paused round was ready or running. */
    private enum State {
        /** Waiting for the first movement key. */
        READY,
        /** Movement timer is active. */
        RUNNING,
        /** Paused before the first movement. */
        PAUSED_READY,
        /** Paused after movement began. */
        PAUSED_RUNNING,
        /** Round ended through collision or victory. */
        ENDED
    }
    private State state = State.READY;

    /**
     * Changes a ready round to running without resuming a paused round.
     */
    void start() { if (state == State.READY) state = State.RUNNING; }
    /**
     * Pauses a ready or running round, preserving which state should resume.
     *
     * @return whether a new pause was entered
     */
    boolean pause() {
        if (state == State.READY) state = State.PAUSED_READY;
        else if (state == State.RUNNING) state = State.PAUSED_RUNNING;
        else return false;
        return true;
    }
    /**
     * Restores the state saved by pause.
     *
     * @return whether the timer should resume running
     */
    boolean resume() {
        if (state == State.PAUSED_RUNNING) { state = State.RUNNING; return true; }
        if (state == State.PAUSED_READY) state = State.READY;
        return false;
    }
    /**
     * Checks both paused UI states.
     *
     * @return whether the round is paused
     */
    boolean isPaused() { return state == State.PAUSED_READY || state == State.PAUSED_RUNNING; }
    /**
     * Marks the UI flow as ended.
     */
    void end() { state = State.ENDED; }
    /**
     * Restores the ready state for a restart or new level.
     */
    void reset() { state = State.READY; }
}
