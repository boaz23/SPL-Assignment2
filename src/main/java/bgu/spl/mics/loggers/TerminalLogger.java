package bgu.spl.mics.loggers;

import java.io.IOException;

public class TerminalLogger implements Logger {
    @Override
    public synchronized Logger appendLine(String s) {
        System.out.println(s);
        return this;
    }

    @Override
    public synchronized Logger append(String s) {
        System.out.print(s);
        return this;
    }

    @Override
    public synchronized Logger appendLine(Object o) {
        System.out.println(o);
        return this;
    }

    @Override
    public synchronized Logger append(Object o) {
        System.out.print(o);
        return this;
    }

    @Override
    public synchronized void flush() throws IOException {
        System.out.flush();
    }
}
