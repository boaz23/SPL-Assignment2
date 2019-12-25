package bgu.spl.mics.loggers;

import java.io.IOException;

public class Loggers {
    public static Logger NoLogger;
    public static Logger StringBufferLogger;
    public static Logger FileLogger;

    public static Logger DefaultLogger;
    public static Logger MI6RunnerLogger;
    public static Logger MnMPLogger;

    static {
        NoLogger = new NoLogger();
        StringBufferLogger = new StringBufferLogger();
        FileLogger = initFileLogger("runs-output/run.log");

        DefaultLogger = NoLogger;
        MI6RunnerLogger = NoLogger;
        MnMPLogger = NoLogger;
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
