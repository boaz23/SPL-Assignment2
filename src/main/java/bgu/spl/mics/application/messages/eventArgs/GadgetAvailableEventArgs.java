package bgu.spl.mics.application.messages.eventArgs;

public class GadgetAvailableEventArgs {
    private final String gadget;
    private boolean isAvailable;

    public GadgetAvailableEventArgs(String gadget) {
        this.gadget = gadget;
    }

    public String getGadget() {
        return gadget;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
