package bgu.spl.mics.application;

import bgu.spl.mics.application.config.*;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.publishers.TimeService;
import bgu.spl.mics.application.subscribers.Intelligence;
import bgu.spl.mics.application.subscribers.M;
import bgu.spl.mics.application.subscribers.Moneypenny;
import bgu.spl.mics.application.subscribers.Q;
import bgu.spl.mics.loggers.Loggers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        Loggers.DefaultLogger = Loggers.NoLogger;
        Iterable<Iterable<Thread>> threads = initialize(config);
        startAll(threads);
        startInterrupter();
        waitForFinish(threads);
        printLogToTerminal();
        if (Thread.currentThread().isInterrupted()) {
            exit();
        } else {
            printOutputToFiles(inventoryOutputFilePath, diaryOutputFilePath);
        }
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

    private static Iterable<Iterable<Thread>> initialize(Config config) {
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

    private static Iterable<Iterable<Thread>> initializeActiveObjects(Config config) {
        Services services = config.services;

        Thread timeServiceThread = initializeTimeService(services);
        Thread[] intelligenceThreads = initializeIntelligences(services);
        Thread qThread = initializeQ(Inventory.getInstance());
        Thread[] mThreads = initializeMs(services, Diary.getInstance());
        Thread[] moneypennyThreads = initializeMoneypennies(services);

        int subCount = intelligenceThreads.length + 1 + mThreads.length + moneypennyThreads.length;
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

        return threads;
    }

    private static Thread initializeTimeService(Services services) {
        TimeService timeService = new TimeService(services.time, "TimeService");
        Thread timeServiceThread = new Thread(timeService);
        timeServiceThread.setName(timeService.getName());
        return timeServiceThread;
    }

    private static Thread[] initializeIntelligences(Services services) {
        bgu.spl.mics.application.config.Intelligence[] intelligenceObjs = services.intelligence;
        Thread[] intelligenceThreads = new Thread[intelligenceObjs.length];
        for (int i = 0; i < intelligenceObjs.length; i++) {
            Intelligence intelligence = initializeIntelligence(intelligenceObjs[i], i + 1);
            intelligenceThreads[i] = initializeIntelligenceThread(intelligence);
        }

        return intelligenceThreads;
    }

    private static Intelligence initializeIntelligence(bgu.spl.mics.application.config.Intelligence intelligenceObj, int id) {
        String name = "Intelligence" + id;
        MissionInfo[] missionInfos = initializeMissionInfos(intelligenceObj.missions);
        return new Intelligence(name, missionInfos);
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

    private static Thread initializeQ(Inventory inventory) {
        Q q = new Q(inventory);
        Thread qThread = new Thread(q);
        qThread.setName(q.getName());
        return qThread;
    }

    private static Thread[] initializeMs(Services services, Diary diary) {
        int count = services.M;
        Thread[] mThreads = new Thread[count];
        for (int i = 0; i < count; i++) {
            M m = new M(i + 1, diary);
            Thread thread = new Thread(m);
            thread.setName(m.getName());
            mThreads[i] = thread;
        }

        return mThreads;
    }

    private static Thread[] initializeMoneypennies(Services services) {
        int count = services.Moneypenny;
        Thread[] moneypennyThreads = new Thread[count];
        initializeMoneypennies(moneypennyThreads);
        return moneypennyThreads;
    }

    private static void initializeMoneypennies(Thread[] moneypennyThreads) {
        int count = moneypennyThreads.length;
        int missionHandlersCount = count / 2;
        int agentManagersCount = count - missionHandlersCount;
        int iNext = 0;
        iNext = initializeMoneypennies(moneypennyThreads, iNext, missionHandlersCount, Moneypenny.SubscribeTO.SendAndRelease);
        iNext = initializeMoneypennies(moneypennyThreads, iNext, agentManagersCount, Moneypenny.SubscribeTO.AgentsAvailable);
    }

    private static int initializeMoneypennies(Thread[] moneypennyThreads, int iStart, int count, Moneypenny.SubscribeTO duty) {
        int iEnd = iStart + count;
        for (int i = iStart; i < iEnd; i++) {
            Moneypenny moneypenny = new Moneypenny(i + 1, duty);
            Thread thread = new Thread(moneypenny);
            thread.setName(moneypenny.getName());
            moneypennyThreads[i] = thread;
        }

        return iEnd;
    }

    private static <T> void addAll(List<T> list, T[] arr) {
        list.addAll(Arrays.asList(arr));
    }

    private static void startAll(Iterable<Iterable<Thread>> threads) {
        for (Iterable<Thread> threadsGroup : threads) {
            for (Thread thread : threadsGroup) {
                thread.start();
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void waitForFinish(Iterable<Iterable<Thread>> threads) {
        try {
            for (Iterable<Thread> threadsGroup : threads) {
                for (Thread thread : threadsGroup) {
                    thread.join();
                }
            }
            Loggers.DefaultLogger.append("\nDone!");
        } catch (InterruptedException e) {
            Loggers.DefaultLogger.append("\nInterrupted while waiting");
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
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

    private static void printLogToTerminal() {
        try {
            Loggers.DefaultLogger.flush();
            System.out.println(Loggers.DefaultLogger);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void exit() {
        try {
            Thread.currentThread().join();
        }
        catch (InterruptedException ignored) {
        }
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
