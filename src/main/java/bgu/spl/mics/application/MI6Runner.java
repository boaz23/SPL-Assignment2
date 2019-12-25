package bgu.spl.mics.application;

import bgu.spl.mics.Tuple;
import bgu.spl.mics.application.config.*;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.publishers.TimeService;
import bgu.spl.mics.application.subscribers.Intelligence;
import bgu.spl.mics.application.subscribers.M;
import bgu.spl.mics.application.subscribers.Moneypenny;
import bgu.spl.mics.application.subscribers.Q;
import bgu.spl.mics.loggers.Logger;
import bgu.spl.mics.loggers.Loggers;
import bgu.spl.mics.loggers.StringBufferLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class MI6Runner {
    private static final int MAX_TICKS_BEFORE_INTERRUPT = 200;

    public static void main(String[] args) {
        if (args.length < 3) {
            printErr("Bad arguments, expected 3 file paths");
            return;
        }

        String configFilePath = args[0];
        String inventoryOutputFilePath = args[1];
        String diaryOutputFilePath = args[2];

        Config config = loadConfig(configFilePath);
        if (config == null) {
            return;
        }

        setLoggers();
        run(config);
        printLogsToTerminal();
        if (!Thread.currentThread().isInterrupted()) {
            printOutputToFiles(inventoryOutputFilePath, diaryOutputFilePath);
        }
    }

    private static void setLoggers() {
        Loggers.StringBufferLogger = new StringBufferLogger();
        Loggers.DefaultLogger = Loggers.StringBufferLogger;
        Loggers.MI6RunnerLogger = Loggers.StringBufferLogger;
        Loggers.MnMPLogger = Loggers.StringBufferLogger;
    }

    private static void run(Config config) {
        Tuple<List<Iterable<Thread>>, CountDownLatch> init = initialize(config);
        List<Iterable<Thread>> splitThreads = init.getFirst();
        CountDownLatch subRegisterAwaiter = init.getSecond();

        Iterable<Thread> threads = startAll(splitThreads, subRegisterAwaiter);
        waitForFinish(threads);
        if (Thread.currentThread().isInterrupted()) {
            logAllAliveThreads(threads);
        }
    }

    private static void logAllAliveThreads(Iterable<Thread> threads) {
        Iterable<Thread> aliveThreads = findAllAlive(threads);
        logThreads(aliveThreads);
    }

    private static Config loadConfig(String configFilePath) {
        try (ConfigLoader configLoader = new ConfigLoader(configFilePath)) {
            return configLoader.init().load();
        }
        catch (FileNotFoundException e) {
            printErr("Input file not found.");
        }
        catch (IOException e) {
            printErr("Unexpected IO exception occured when parsing the input file");
            e.printStackTrace();
        }

        return null;
    }

    private static Tuple<List<Iterable<Thread>>, CountDownLatch> initialize(Config config) {
        loadInventory(config);
        loadSquad(config);
        return initializeActiveObjects(config);
    }

    private static void loadInventory(Config config) {
        Inventory.getInstance().load(config.inventory);
    }

    private static void loadSquad(Config config) {
        bgu.spl.mics.application.config.Agent[] agentObjs = config.squad;
        Agent[] agents = new Agent[agentObjs.length];
        for (int i = 0; i < agentObjs.length; i++) {
            Agent agent = new Agent();
            agent.setName(agentObjs[i].name);
            agent.setSerialNumber(agentObjs[i].serialNumber);
            agents[i] = agent;
        }

        Squad.getInstance().load(agents);
    }

    private static Tuple<List<Iterable<Thread>>, CountDownLatch> initializeActiveObjects(Config config) {
        Services services = config.services;
        int subCount = services.intelligence.length + 1 + services.M + services.Moneypenny;
        CountDownLatch subRegisterAwaiter = new CountDownLatch(subCount);

        Thread timeServiceThread = initializeTimeService(services);
        Thread[] intelligenceThreads = initializeIntelligences(services, subRegisterAwaiter);
        Thread qThread = initializeQ(Inventory.getInstance(), subRegisterAwaiter);
        Thread[] mThreads = initializeMs(services, Diary.getInstance(), subRegisterAwaiter);
        Thread[] moneypennyThreads = initializeMoneypennies(services, subRegisterAwaiter);

        ArrayList<Thread> subscriberThreads = new ArrayList<>(subCount);
        subscriberThreads.add(qThread);
        addAll(subscriberThreads, moneypennyThreads);
        addAll(subscriberThreads, mThreads);
        addAll(subscriberThreads, intelligenceThreads);

        ArrayList<Thread> publisherThreads = new ArrayList<Thread>(1) {{
           add(timeServiceThread);
        }};

        ArrayList<Iterable<Thread>> threads = new ArrayList<Iterable<Thread>>(2) {{
            add(subscriberThreads);
            add(publisherThreads);
        }};
        return new Tuple<>(threads, subRegisterAwaiter);
    }

    private static Thread initializeTimeService(Services services) {
        TimeService timeService = new TimeService(services.time, "TimeService");
        Thread timeServiceThread = new Thread(timeService);
        timeServiceThread.setName(timeService.getName());
        return timeServiceThread;
    }

    private static Thread[] initializeIntelligences(Services services, CountDownLatch subRegisterAwaiter) {
        bgu.spl.mics.application.config.Intelligence[] intelligenceObjs = services.intelligence;
        Thread[] intelligenceThreads = new Thread[intelligenceObjs.length];
        for (int i = 0; i < intelligenceObjs.length; i++) {
            Intelligence intelligence = initializeIntelligence(intelligenceObjs[i], i + 1, subRegisterAwaiter);
            intelligenceThreads[i] = initializeIntelligenceThread(intelligence);
        }

        return intelligenceThreads;
    }

    private static Intelligence initializeIntelligence(bgu.spl.mics.application.config.Intelligence intelligenceObj, int id, CountDownLatch subRegisterAwaiter) {
        String name = "Intelligence" + id;
        MissionInfo[] missionInfos = initializeMissionInfos(intelligenceObj.missions);
        return new Intelligence(name, missionInfos, subRegisterAwaiter);
    }

    private static Thread initializeIntelligenceThread(Intelligence intelligence) {
        Thread thread = new Thread(intelligence);
        thread.setName(intelligence.getName());
        return thread;
    }

    private static MissionInfo[] initializeMissionInfos(Mission[] missions) {
        MissionInfo[] missionInfos = new MissionInfo[missions.length];
        for (int i = 0; i < missions.length; i++) {
            MissionInfo missionInfo = new MissionInfo();
            missionInfo.setMissionName(missions[i].name);
            missionInfo.setSerialAgentsNumbers(initializeAgentsSerialNumbers(missions[i]));
            missionInfo.setGadget(missions[i].gadget);
            missionInfo.setTimeIssued(missions[i].timeIssued);
            missionInfo.setTimeExpired(missions[i].timeExpired);
            missionInfo.setDuration(missions[i].duration);
            missionInfos[i] = missionInfo;
        }

        return missionInfos;
    }

    private static List<String> initializeAgentsSerialNumbers(Mission mission) {
        return Arrays.asList(mission.serialAgentsNumbers);
    }

    private static Thread initializeQ(Inventory inventory, CountDownLatch subRegisterAwaiter) {
        Q q = new Q("Q", inventory, subRegisterAwaiter);
        Thread qThread = new Thread(q);
        qThread.setName(q.getName());
        return qThread;
    }

    private static Thread[] initializeMs(Services services, Diary diary, CountDownLatch subRegisterAwaiter) {
        int count = services.M;
        Thread[] mThreads = new Thread[count];
        for (int i = 0; i < count; i++) {
            M m = new M(i + 1, diary, subRegisterAwaiter);
            Thread thread = new Thread(m);
            thread.setName(m.getName());
            mThreads[i] = thread;
        }

        return mThreads;
    }

    private static Thread[] initializeMoneypennies(Services services, CountDownLatch subRegisterAwaiter) {
        int count = services.Moneypenny;
        Thread[] moneypennyThreads = new Thread[count];
        initializeMoneypennies(moneypennyThreads, subRegisterAwaiter);
        return moneypennyThreads;
    }

    private static void initializeMoneypennies(Thread[] moneypennyThreads, CountDownLatch subRegisterAwaiter) {
        int count = moneypennyThreads.length;
        int missionHandlersCount = (count / 2);
        int agentManagersCount = count - missionHandlersCount;
        Moneypenny.Releaser releaser = new Moneypenny.Releaser(agentManagersCount);

        int iNext = 0;
        iNext = initializeMoneypennies(moneypennyThreads, iNext, missionHandlersCount, releaser, Moneypenny.SubscribeTO.SendAndRelease, subRegisterAwaiter);
        iNext = initializeMoneypennies(moneypennyThreads, iNext, agentManagersCount, releaser, Moneypenny.SubscribeTO.AgentsAvailable, subRegisterAwaiter);
    }

    private static int initializeMoneypennies(Thread[] moneypennyThreads, int iStart, int count, Moneypenny.Releaser releaser, Moneypenny.SubscribeTO duty, CountDownLatch subRegisterAwaiter) {
        int iEnd = iStart + count;
        for (int i = iStart; i < iEnd; i++) {
            Moneypenny moneypenny = new Moneypenny(i + 1, releaser, duty, subRegisterAwaiter);
            Thread thread = new Thread(moneypenny);
            thread.setName(moneypenny.getName());
            moneypennyThreads[i] = thread;
        }

        return iEnd;
    }

    private static <T> void addAll(List<T> list, T[] arr) {
        list.addAll(Arrays.asList(arr));
    }

    private static Iterable<Thread> startAll(List<Iterable<Thread>> splitThreads, CountDownLatch subRegisterAwaiter) {
        ArrayList<Thread> threads = new ArrayList<>();
        for (Iterable<Thread> threadsGroup : splitThreads) {
            for (Thread thread : threadsGroup) {
                threads.add(thread);
                thread.start();
            }

            try {
                // Allow every subscriber to register before the publishers
                subRegisterAwaiter.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return threads;
    }

    private static void waitForFinish(Iterable<Thread> threads) {
        try {
            for (Thread thread : threads) {
                thread.join();
            }
            Loggers.MI6RunnerLogger.appendLine("\nDone!");
        } catch (InterruptedException e) {
            Loggers.MI6RunnerLogger.appendLine("\nInterrupted while waiting");
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private static Iterable<Thread> findAllAlive(Iterable<Thread> threads) {
        ArrayList<Thread> stillAlive = new ArrayList<>();
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                stillAlive.add(thread);
            }
        }

        return stillAlive;
    }

    private static void logThreads(Iterable<Thread> threads) {
        Loggers.MI6RunnerLogger.append("\nPrinting alive threads: [");
        Iterator<Thread> iterator = threads.iterator();
        if (iterator.hasNext()) {
            Loggers.MI6RunnerLogger.appendLine("");
            Thread next = iterator.next();
            while (iterator.hasNext()) {
                appendThreadName(next);
                Loggers.MI6RunnerLogger.appendLine(",");
                next = iterator.next();
            }
            appendThreadName(next);
            Loggers.MI6RunnerLogger.appendLine("");
        }
        Loggers.MI6RunnerLogger.append("]");
    }

    private static void appendThreadName(Thread next) {
        Loggers.MI6RunnerLogger.append("    " + next.getName());
    }

    private static void startInterrupter() {
        Thread mainThread = Thread.currentThread();
        Thread interrupter = new Thread(() -> {
            try {
                Thread.sleep(MAX_TICKS_BEFORE_INTERRUPT * TimeService.getTimeTickDuration());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mainThread.interrupt();
        });
        interrupter.start();
    }

    private static void printOutputToFiles(String inventoryOutputFilePath, String diaryOutputFilePath) {
        System.out.println("Printing output to files...");
        printInventoryToFile(inventoryOutputFilePath);
        printDiaryToFile(diaryOutputFilePath);
    }

    private static void printLogsToTerminal() {
        printLogToTerminal(Loggers.DefaultLogger);
        printLogToTerminal(Loggers.StringBufferLogger);
    }

    private static void printLogToTerminal(Logger logger) {
        try {
            logger.flush();
            System.out.println(logger);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void exit() {
        System.exit(1);
    }

    private static void printInventoryToFile(String inventoryOutputFilePath) {
        Inventory.getInstance().printToFile(inventoryOutputFilePath);
    }

    private static void printDiaryToFile(String diaryOutputFilePath) {
        Diary.getInstance().printToFile(diaryOutputFilePath);
    }

    private static void printErr(String msg) {
        System.out.println(msg);
        System.err.println(msg);
    }
}
