package bgu.spl.mics.application.messages.eventsInfo;

import bgu.spl.mics.application.messages.SendAgentsEvent;

import java.util.List;

/**
 * The value of
 * @see SendAgentsEvent
 */
public class SendAgentsEventArgs {
    private final List<String> serialAgentsNumbers;
    private final int duration;
    private final String missionName;

    /**
     * Initializes a new instance
     * @param serialAgentsNumbers The serial numbers of agents
     * @param duration The mission duration time
     */
    public SendAgentsEventArgs(List<String> serialAgentsNumbers, int duration, String missionName) {
        this.serialAgentsNumbers = serialAgentsNumbers;
        this.duration = duration;
        this.missionName = missionName;
    }

    /**
     * @return The serial numbers of agents which are requested to be released
     */
    public List<String> serialAgentsNumbers() {
        return serialAgentsNumbers;
    }

    /**
     * @return The duration of the mission
     */
    public int duration() {
        return duration;
    }

    /**
     * @return The mission's name
     */
    public String getMissionName() {
        return missionName;
    }
}
