package bgu.spl.mics.loggers;

public class NonLogger implements Logger {
    @Override
    public Logger appendLine(String s) {
        return this;
    }

    @Override
    public Logger append(String s) {
        return this;
    }

    @Override
    public Logger appendLine(Object o) {
        return this;
    }

    @Override
    public Logger append(Object o) {
        return this;
    }

    @Override
    public String toString() {
        return "Empty logger";
    }
}
