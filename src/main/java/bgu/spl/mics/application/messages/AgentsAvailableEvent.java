package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Utils;
import bgu.spl.mics.application.messages.eventsInfo.AgentsAvailableEventArgs;
import bgu.spl.mics.application.messages.eventsInfo.AgentsAvailableResult;

/**
 * The event to be sent when Agents are needed to be acquired for a mission
 */
public class AgentsAvailableEvent implements Event<AgentsAvailableResult> {
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + Utils.listToString(args.agentsSerialNumbers());
    }
}
