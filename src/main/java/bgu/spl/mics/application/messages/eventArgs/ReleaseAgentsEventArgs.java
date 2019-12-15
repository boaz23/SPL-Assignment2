package bgu.spl.mics.application.messages.eventArgs;

import java.util.List;

public class ReleaseAgentsEventArgs {
    private List<String> serialAgentsNumbers;

    public ReleaseAgentsEventArgs(List<String> serialAgentsNumbers) {
        this.serialAgentsNumbers = serialAgentsNumbers;
    }

    public List<String> serialAgentsNumbers() {
        return serialAgentsNumbers;
    }
}
