package bgu.spl.mics.application.messages.eventsInfo;

import bgu.spl.mics.application.messages.GadgetAvailableEvent;

/**
 * The value of
 * @see GadgetAvailableEvent
 */
public class GadgetAvailableResult {
    private final boolean isAvailable;
    private final int receiveTime;
    private final int qSerialNumber;

    /**
     * Initializes a new instance
     * @param isAvailable The value for whether the gadget is available
     * @param receiveTime The time tick in which the Q instance who handled the request received it
     * @param qSerialNumber The serial number of the Q instance who handled the request
     */
    public GadgetAvailableResult(boolean isAvailable, int receiveTime, int qSerialNumber) {
        this.isAvailable = isAvailable;
        this.receiveTime = receiveTime;
        this.qSerialNumber = qSerialNumber;
    }

    /**
     * @return Whether the requested gadget is available
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * @return
     */
    public int qSerialNumber() {
        return qSerialNumber;
    }

    /**
     * @return The time tick in which the Q instance who handled the request received it
     */
    public int receiveTime() {
        return receiveTime;
    }
}
