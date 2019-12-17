package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.eventArgs.ReleaseAgentsEventArgs;

/**
 * The event to be sent when agents should be released from a mission (whether it was cancelled or not)
 */
public class ReleaseAgentsEvent implements Event<ReleaseAgentsEventArgs> {
    private final ReleaseAgentsEventArgs args;

    /**
     * Initializes a new instance with the given args
     * @param args The arguments for this event
     */
    public ReleaseAgentsEvent(ReleaseAgentsEventArgs args) {
        this.args = args;
    }

    /**
     * @return The arguments for this event
     */
    public ReleaseAgentsEventArgs getArgs() {
        return args;
    }
}
