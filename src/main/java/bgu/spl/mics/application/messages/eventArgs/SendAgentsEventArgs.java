package bgu.spl.mics.application.messages.eventArgs;

import java.util.List;

public class SendAgentsEventArgs {
    private List<String> serialAgentsNumbers;
    private int duration;

    public SendAgentsEventArgs(List<String> serialAgentsNumbers, int duration) {
        this.serialAgentsNumbers = serialAgentsNumbers;
        this.duration = duration;
    }

    public List<String> serialAgentsNumbers() {
        return serialAgentsNumbers;
    }

    public int duration() {
        return duration;
    }
}
