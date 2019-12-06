package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class FutureTest {
    private Future<Integer> future;

    @BeforeEach
    public void setUp() {
        future = new Future<>();
    }

    @Test
    public void simpleResolveGet() {
        assertFalse(future.isDone(), "Done before resolved");
        future.resolve(5);
        assertEquals(5, future.get(), "Different result after resolve");
        assertTrue(future.isDone(), "Not done after resolve");
    }

    @Test
    public void timedResolveGet() {
        assertFalse(future.isDone(), "Done before resolved");
        future.resolve(5);
        assertEquals(5, future.get(10, TimeUnit.MILLISECONDS), "Different result after resolve");
        assertTrue(future.isDone(), "Not done after resolve");
    }

    @Test
    public void blockingGet() {
        assertFalse(future.isDone(), "Done before resolved");
        Thread resolver = new Thread(() -> {
            try {
                Thread.sleep(20);
                future.resolve(7);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        resolver.start();
        assertEquals(7, future.get(), "Different result after resolve");
        assertTrue(future.isDone(), "Not done after resolve");
        try {
            resolver.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void timedGet_notWaitingTilTimoutWhenResolved() {
        assertFalse(future.isDone(), "Done before resolved");
        Thread resolver = new Thread(() -> {
            try {
                Thread.sleep(20);
                future.resolve(7);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        resolver.start();

        long start = System.currentTimeMillis();
        Integer result = future.get(50, TimeUnit.MILLISECONDS);
        long end = System.currentTimeMillis();
        long duration = end - start;

        assertEquals(7, result, "Different result after resolve");
        System.out.println("duration: " + duration);
        assertTrue(20 <= duration && duration <= 22, "Blocked for longer than neeeded");
        assertTrue(future.isDone(), "Not done after resolve");
        try {
            resolver.interrupt();
            resolver.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void timedGet_timeout() {
        assertFalse(future.isDone(), "Done before resolved");
        Thread resolver = new Thread(() -> {
            try {
                Thread.sleep(50);
                future.resolve(7);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        resolver.start();

        long start = System.currentTimeMillis();
        Integer result = future.get(30, TimeUnit.MILLISECONDS);
        long end = System.currentTimeMillis();
        long duration = end - start;

        assertNull(result, "Got some result despite timeout");
        assertTrue(30 <= duration && duration <= 32, "Blocked for longer than neeeded");
        assertFalse(future.isDone(), "Done despite timeout");

        try {
            resolver.interrupt();
            resolver.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
