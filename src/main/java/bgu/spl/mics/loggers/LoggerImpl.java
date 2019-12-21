package bgu.spl.mics.loggers;

/**
 * Responsible for logging to a string buffer
 */
public class LoggerImpl implements Logger {
    private StringBuilder sb;

    /**
     * Initializes a new logger instance with a default StringBuffer
     */
    public LoggerImpl() {
        sb = new StringBuilder();
    }

    public synchronized LoggerImpl appendLine(String s) {
        sb.append(s)
            .append('\n');
        return this;
    }

    public synchronized LoggerImpl append(String s) {
        sb.append(s);
        return this;
    }

    public synchronized LoggerImpl appendLine(Object o) {
        sb.append(o)
            .append('\n');
        return this;
    }

    public synchronized LoggerImpl append(Object o) {
        sb.append(o);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
