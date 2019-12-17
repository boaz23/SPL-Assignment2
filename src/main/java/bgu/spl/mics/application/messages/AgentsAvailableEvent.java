package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.eventArgs.AgentsAvailableEventArgs;

/**
 * The event to be sent when Agents are needed to be acquired for a mission
 */
public class AgentsAvailableEvent implements Event<AgentsAvailableEventArgs> {
    private final AgentsAvailableEventArgs args;

    /**
     * Initializes a new instance with the given args
     * @param args The arguments for this event
     */
    public AgentsAvailableEvent(AgentsAvailableEventArgs args) {
        this.args = args;
    }

    /**
     * @return The arguments for this event
     */
    public AgentsAvailableEventArgs getArgs() {
        return args;
    }
}
