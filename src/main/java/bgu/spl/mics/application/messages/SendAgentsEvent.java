package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.eventArgs.SendAgentsEventArgs;

public class SendAgentsEvent implements Event<SendAgentsEventArgs> {
    private final SendAgentsEventArgs args;

    public SendAgentsEvent(SendAgentsEventArgs args) {
        this.args = args;
    }

    public SendAgentsEventArgs getArgs() {
        return args;
    }
}
