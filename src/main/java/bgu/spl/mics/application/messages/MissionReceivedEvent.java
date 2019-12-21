package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

/**
 * The event to be sent when a new mission should be issued
 */
public class MissionReceivedEvent implements Event<Void> {
    private final MissionInfo missionInfo;

    /**
     * Initializes a new instance with the given information about the mission
     * @param missionInfo The mission's information
     */
    public MissionReceivedEvent(MissionInfo missionInfo) {
        this.missionInfo = missionInfo;
    }

    /**
     * @return The information about the mission
     */
    public MissionInfo getMissionInfo() {
        return missionInfo;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": '" + missionInfo.getMissionName() + "'";
    }
}
