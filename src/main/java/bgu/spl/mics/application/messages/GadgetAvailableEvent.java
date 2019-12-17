package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.eventArgs.GadgetAvailableEventArgs;

public class GadgetAvailableEvent implements Event<GadgetAvailableEventArgs> {
    private final GadgetAvailableEventArgs args;

    public GadgetAvailableEvent(GadgetAvailableEventArgs args) {
        this.args = args;
    }

    public GadgetAvailableEventArgs getArgs() {
        return args;
    }
}
