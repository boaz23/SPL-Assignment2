package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.eventArgs.GadgetAvailableEventArgs;

/**
 * The event to be sent when a gadget is need for a mission in order to know whether it's available
 */
public class GadgetAvailableEvent implements Event<GadgetAvailableEventArgs> {
    private final GadgetAvailableEventArgs args;

    /**
     * Initializes a new instance with the given args
     * @param args The arguments for this event
     */
    public GadgetAvailableEvent(GadgetAvailableEventArgs args) {
        this.args = args;
    }

    /**
     * @return The arguments for this event
     */
    public GadgetAvailableEventArgs getArgs() {
        return args;
    }
}
