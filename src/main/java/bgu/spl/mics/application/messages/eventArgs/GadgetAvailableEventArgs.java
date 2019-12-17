package bgu.spl.mics.application.messages.eventArgs;

import bgu.spl.mics.application.messages.GadgetAvailableEvent;

/**
 * The value of
 * @see GadgetAvailableEvent
 */
public class GadgetAvailableEventArgs {
    private final String gadget;
    private boolean isAvailable;

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

    /**
     * @return Whether the requested gadget is available
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * Sets whether the requested gadget was available
     * @param available The value to set to
     */
    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
