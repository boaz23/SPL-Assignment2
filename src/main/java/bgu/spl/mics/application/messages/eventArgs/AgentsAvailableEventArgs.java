package bgu.spl.mics.application.messages.eventArgs;

import bgu.spl.mics.application.messages.AgentsAvailableEvent;

import java.util.List;

/**
 * The value of
 * @see AgentsAvailableEvent
 */
public class AgentsAvailableEventArgs {
    private final List<String> agentsSerialNumbers;
    private boolean areSerialAgentsNumbersValid;

    /**
     * Initializes a new instance with the given agents serial numbers which are needed for a mission
     * @param agentsSerialNumbers The agents serial numbers
     */
    public AgentsAvailableEventArgs(List<String> agentsSerialNumbers) {
        this.agentsSerialNumbers = agentsSerialNumbers;
    }

    /**
     * @return Whether acquiring the agents succeeded
     */
    public boolean areSerialAgentsNumbersValid() {
        return areSerialAgentsNumbersValid;
    }

    /**
     * Sets whether getting the agents was a valid operation
     * @param areSerialAgentsNumbersValid The value to set to
     */
    public void setAreSerialAgentsNumbersValid(boolean areSerialAgentsNumbersValid) {
        this.areSerialAgentsNumbersValid = areSerialAgentsNumbersValid;
    }

    /**
     * @return The list of agents needed for the mission
     */
    public List<String> getAgentsSerialNumbers() {
        return agentsSerialNumbers;
    }
}
