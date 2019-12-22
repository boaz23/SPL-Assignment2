package bgu.spl.mics.loggers;

import java.io.IOException;

public class NoLogger implements Logger {
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
    public void flush() throws IOException {

    }

    @Override
    public String toString() {
        return "Empty logger";
    }
}
