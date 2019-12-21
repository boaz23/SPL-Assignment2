package bgu.spl.mics.application.messages;

public class LastTickBroadcast extends TickBroadcast {
    /**
     * Initializes a new instance with the current time tick
     *
     * @param tick The current time tick
     */
    public LastTickBroadcast(int tick) {
        super(tick);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
