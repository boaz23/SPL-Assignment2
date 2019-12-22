package bgu.spl.mics.loggers;

import java.io.IOException;

/**
 * Responsible for logging to a string buffer
 */
public class StringBufferLogger implements Logger {
    private StringBuilder sb;

    /**
     * Initializes a new logger instance with a default StringBuffer
     */
    public StringBufferLogger() {
        sb = new StringBuilder();
    }

    public synchronized StringBufferLogger appendLine(String s) {
        sb.append(s)
            .append('\n');
        return this;
    }

    public synchronized StringBufferLogger append(String s) {
        sb.append(s);
        return this;
    }

    public synchronized StringBufferLogger appendLine(Object o) {
        sb.append(o)
            .append('\n');
        return this;
    }

    public synchronized StringBufferLogger append(Object o) {
        sb.append(o);
        return this;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
