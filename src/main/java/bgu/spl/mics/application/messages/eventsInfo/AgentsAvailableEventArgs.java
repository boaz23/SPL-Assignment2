package bgu.spl.mics.application.messages.eventsInfo;

import bgu.spl.mics.application.messages.AgentsAvailableEvent;

import java.util.List;

/**
 * The arguments for
 * @see AgentsAvailableEvent
 */
public class AgentsAvailableEventArgs {
    private final List<String> agentsSerialNumbers;

    /**
     * Initializes a new instance with the given agents serial numbers which are needed for a mission
     * @param agentsSerialNumbers The agents serial numbers
     */
    public AgentsAvailableEventArgs(List<String> agentsSerialNumbers) {
        this.agentsSerialNumbers = agentsSerialNumbers;
    }

    /**
     * @return The list of agents needed for the mission
     */
    public List<String> getAgentsSerialNumbers() {
        return agentsSerialNumbers;
    }
}
