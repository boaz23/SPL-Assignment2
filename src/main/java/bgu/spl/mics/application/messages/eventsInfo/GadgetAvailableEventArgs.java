package bgu.spl.mics.application.messages.eventsInfo;

import bgu.spl.mics.application.messages.GadgetAvailableEvent;

/**
 * The arguments for
 * @see GadgetAvailableEvent
 */
public class GadgetAvailableEventArgs {
    private final String gadget;

    /**
     * Initializes a new instance with a given gadget needed for a mission
     * @param gadget
     */
    public GadgetAvailableEventArgs(String gadget) {
        this.gadget = gadget;
    }

    /**
     * @return The gadget needed for a mission
     */
    public String getGadget() {
        return gadget;
    }
}
