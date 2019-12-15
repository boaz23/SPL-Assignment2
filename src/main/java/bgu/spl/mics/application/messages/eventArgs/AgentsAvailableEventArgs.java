package bgu.spl.mics.application.messages.eventArgs;

public class AgentsAvailableEventArgs {
    private boolean areSerialAgentsNumbersValid;

    public AgentsAvailableEventArgs(boolean areSerialAgentsNumbersValid) {
        this.areSerialAgentsNumbersValid = areSerialAgentsNumbersValid;
    }

    public boolean areSerialAgentsNumbersValid() {
        return areSerialAgentsNumbersValid;
    }
}
