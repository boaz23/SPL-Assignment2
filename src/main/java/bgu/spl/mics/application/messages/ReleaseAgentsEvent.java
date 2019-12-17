package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.eventArgs.ReleaseAgentsEventArgs;

public class ReleaseAgentsEvent implements Event<ReleaseAgentsEventArgs> {
    private final ReleaseAgentsEventArgs args;

    public ReleaseAgentsEvent(ReleaseAgentsEventArgs args) {
        this.args = args;
    }

    public ReleaseAgentsEventArgs getArgs() {
        return args;
    }
}
