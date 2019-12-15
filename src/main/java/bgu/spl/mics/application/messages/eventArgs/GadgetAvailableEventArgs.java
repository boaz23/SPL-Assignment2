package bgu.spl.mics.application.messages.eventArgs;

public class GadgetAvailableEventArgs {
    private boolean isAvailable;

    public GadgetAvailableEventArgs(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
