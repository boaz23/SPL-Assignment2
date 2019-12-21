package bgu.spl.mics.loggers;

import java.io.IOException;

public class Loggers {
    public static final Logger NoLogger;
    public static final Logger StringBufferLogger;
    public static final Logger FileLogger;
    public static Logger DefaultLogger;

    static {
        NoLogger = new NoLogger();
        StringBufferLogger = new StringBufferLogger();
        FileLogger = initFileLogger("runs-output\\log.log");

        DefaultLogger = StringBufferLogger;
    }

    private static Logger initFileLogger(String filePath) {
        try {
            return new FileLogger(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
