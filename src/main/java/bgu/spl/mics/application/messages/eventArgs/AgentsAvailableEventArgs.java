package bgu.spl.mics.application.messages.eventArgs;

import java.util.List;

public class AgentsAvailableEventArgs {
    private final List<String> agentsSerialNumbers;
    private boolean areSerialAgentsNumbersValid;

    public AgentsAvailableEventArgs(List<String> agentsSerialNumbers) {
        this.agentsSerialNumbers = agentsSerialNumbers;
    }

    public boolean areSerialAgentsNumbersValid() {
        return areSerialAgentsNumbersValid;
    }

    public void setAreSerialAgentsNumbersValid(boolean areSerialAgentsNumbersValid) {
        this.areSerialAgentsNumbersValid = areSerialAgentsNumbersValid;
    }

    public List<String> getAgentsSerialNumbers() {
        return agentsSerialNumbers;
    }
}
