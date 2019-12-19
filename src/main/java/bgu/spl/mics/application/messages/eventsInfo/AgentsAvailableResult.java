package bgu.spl.mics.application.messages.eventsInfo;

import bgu.spl.mics.application.messages.AgentsAvailableEvent;

import java.util.List;

/**
 * The value of
 * @see AgentsAvailableEvent
 */
public class AgentsAvailableResult {
    private final boolean areSerialAgentsNumbersValid;
    private final List<String> agentNames;
    private final int moneyPennySerialNumber;

    /**
     * Initializes a new instance
     * @param areSerialAgentsNumbersValid The value for the validity of the agents available request
     * @param agentNames The names of the agents requested for the mission
     * @param moneyPennySerialNumber The serial number of the moneypenny instance who handled the request
     */
    public AgentsAvailableResult(boolean areSerialAgentsNumbersValid, List<String> agentNames, int moneyPennySerialNumber) {
        this.areSerialAgentsNumbersValid = areSerialAgentsNumbersValid;
        this.agentNames = agentNames;
        this.moneyPennySerialNumber = moneyPennySerialNumber;
    }

    /**
     * @return Whether acquiring the agents succeeded
     */
    public boolean areSerialAgentsNumbersValid() {
        return areSerialAgentsNumbersValid;
    }

    /**
     * @return The serial number of the moneypenny instance who handled the request
     */
    public int moneyPennySerialNumber() {
        return moneyPennySerialNumber;
    }

    /**
     * @return The names of the agents requested for the mission
     */
    public List<String> getAgentNames() {
        return agentNames;
    }
}
