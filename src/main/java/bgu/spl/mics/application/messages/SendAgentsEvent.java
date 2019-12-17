package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.eventArgs.SendAgentsEventArgs;

/**
 * The event to be sent when a mission is ready for operation and agents need to be sent to get it done
 */
public class SendAgentsEvent implements Event<SendAgentsEventArgs> {
    private final SendAgentsEventArgs args;

    /**
     * Initializes a new instance with the given args
     * @param args The arguments for this event
     */
    public SendAgentsEvent(SendAgentsEventArgs args) {
        this.args = args;
    }

    /**
     * @return The arguments for this event
     */
    public SendAgentsEventArgs getArgs() {
        return args;
    }
}
