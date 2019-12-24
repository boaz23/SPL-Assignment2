package bgu.spl.mics;

public class TestUtils {
    /**
     * Interrupts the thread and makes sure (gracefully) it fully shuts down before proceeding
     * @param thread The thread to close
     */
    public static void closeThread(Thread thread) {
        try {
            thread.interrupt();
            thread.join();
        } catch (InterruptedException ignored) {
        }
    }
}
