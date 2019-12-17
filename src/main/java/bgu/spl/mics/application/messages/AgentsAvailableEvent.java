package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.eventArgs.AgentsAvailableEventArgs;

public class AgentsAvailableEvent implements Event<AgentsAvailableEventArgs> {
    private final AgentsAvailableEventArgs args;

    public AgentsAvailableEvent(AgentsAvailableEventArgs args) {
        this.args = args;
    }

    public AgentsAvailableEventArgs getArgs() {
        return args;
    }
}
