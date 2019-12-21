package bgu.spl.mics.application.messages.eventsInfo;

import bgu.spl.mics.Utils;
import bgu.spl.mics.application.messages.AgentsAvailableEvent;

import java.util.List;

/**
 * The value of
 * @see AgentsAvailableEvent
 */
public class AgentsAvailableResult {
    private final boolean areSerialAgentsNumbersValid;
    private final List<String> agentNames;
    private final List<String> serialAgentsNumbers;
    private final int moneyPennySerialNumber;

    /**
     * Initializes a new instance
     * @param areSerialAgentsNumbersValid The value for the validity of the agents available request
     * @param agentNames The names of the agents requested for the mission
     * @param moneyPennySerialNumber The serial number of the moneypenny instance who handled the request
     */
    public AgentsAvailableResult(boolean areSerialAgentsNumbersValid, List<String> agentNames, List<String> serialAgentsNumbers, int moneyPennySerialNumber) {
        this.areSerialAgentsNumbersValid = areSerialAgentsNumbersValid;
        this.agentNames = agentNames;
        this.serialAgentsNumbers = serialAgentsNumbers;
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

    /**
     * @return The serial numbers of the agents requested for the mission
     */
    public List<String> getSerialAgentsNumbers() {
        return serialAgentsNumbers;
    }

    @Override
    public String toString() {
        String s = getClass().getSimpleName() + ": ";
        s += Utils.listToString(getSerialAgentsNumbers());
        if (areSerialAgentsNumbersValid()) {
            s += " acquired";
        }
        else {
            s += " invalid";
        }

        return s;
    }
}
