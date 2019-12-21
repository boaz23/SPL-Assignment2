package bgu.spl.mics.application;

import bgu.spl.mics.loggers.FileLogger;
import bgu.spl.mics.loggers.Logger;
import bgu.spl.mics.loggers.StringBufferLogger;

import java.io.IOException;

public class Loggers {
    public static Logger DefaultLogger;

    static {
        DefaultLogger = new StringBufferLogger();
    }
}
