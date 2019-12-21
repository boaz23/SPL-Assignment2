package bgu.spl.mics.application;

import bgu.spl.mics.loggers.Logger;
import bgu.spl.mics.loggers.LoggerImpl;
import bgu.spl.mics.loggers.NonLogger;

public class Loggers {
    public static final Logger DefaultLogger = new LoggerImpl();
    public static final Logger SubscriberMessages = new LoggerImpl();
}
