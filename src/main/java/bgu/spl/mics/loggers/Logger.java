package bgu.spl.mics.loggers;

import java.io.IOException;

/**
 * Responsible for logging to a string buffer
 */
public interface Logger {
    /**
     * Logs the string to the buffer and appends a new line separator
     * @param s The string to append
     * @return The logger instance
     */
    Logger appendLine(String s);

    /**
     * Logs the string to the buffer
     * @param s The string to append
     * @return The logger instance
     */
    Logger append(String s);

    /**
     * Logs the object to the buffer and appends a new line separator
     * @param o The object to append
     * @return The logger instance
     */
    Logger appendLine(Object o);

    /**
     * Logs the string to the buffer
     * @param o The object to append
     * @return The logger instance
     */
    Logger append(Object o);

    void flush() throws IOException;

    @Override
    String toString();
}
