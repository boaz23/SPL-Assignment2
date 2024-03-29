package bgu.spl.mics.application.messages.eventsInfo;

import bgu.spl.mics.application.messages.ReleaseAgentsEvent;

import java.util.List;

/**
 * The value of
 * @see ReleaseAgentsEvent
 */
public class ReleaseAgentsEventArgs {
    private final List<String> serialAgentsNumbers;

    /**
     * Initializes a new instance
     * @param serialAgentsNumbers The serial numbers of the agents
     */
    public ReleaseAgentsEventArgs(List<String> serialAgentsNumbers) {
        this.serialAgentsNumbers = serialAgentsNumbers;
    }

    /**
     * @return The serial numbers of agents which are requested to be released
     */
    public List<String> serialAgentsNumbers() {
        return serialAgentsNumbers;
    }
}
