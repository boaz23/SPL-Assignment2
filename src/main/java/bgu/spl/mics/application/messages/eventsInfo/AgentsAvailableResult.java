package bgu.spl.mics.application.messages.eventsInfo;

import bgu.spl.mics.application.messages.AgentsAvailableEvent;

/**
 * The value of
 * @see AgentsAvailableEvent
 */
public class AgentsAvailableResult {
    private boolean areSerialAgentsNumbersValid;
    /**
     * Initializes a new instance with the given value for the validity of the
     * agents available request
     * @param areSerialAgentsNumbersValid The value for the validity of the agents available request
     */
    public AgentsAvailableResult(boolean areSerialAgentsNumbersValid) {
        this.areSerialAgentsNumbersValid = areSerialAgentsNumbersValid;
    }

    /**
     * @return Whether acquiring the agents succeeded
     */
    public boolean areSerialAgentsNumbersValid() {
        return areSerialAgentsNumbersValid;
    }
}
