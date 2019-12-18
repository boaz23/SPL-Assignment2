package bgu.spl.mics.application.messages.eventsInfo;

import bgu.spl.mics.application.messages.GadgetAvailableEvent;

/**
 * The value of
 * @see GadgetAvailableEvent
 */
public class GadgetAvailableResult {
    private boolean isAvailable;

    /**
     * Initializes a new instance with the given value for whether the gadget is available
     * @param isAvailable The value for whether the gadget is available
     */
    public GadgetAvailableResult(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    /**
     * @return Whether the requested gadget is available
     */
    public boolean isAvailable() {
        return isAvailable;
    }
}
