package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * The broadcast sent very system time tick
 */
public class TickBroadcast implements Broadcast {
    private final int tick;

    /**
     * Initializes a new instance with the current time tick
     * @param tick The current time tick
     */
    public TickBroadcast(int tick) {
        this.tick = tick;
    }

    /**
     * @return The current time tick
     */
    public int getTick() {
        return tick;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + tick;
    }
}
