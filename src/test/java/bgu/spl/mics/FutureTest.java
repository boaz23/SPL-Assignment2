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
    /*
    Tests that trying to fetch the result after resolving returns immediately (using indefinitely blocking get)
     */
    public void simpleResolveGet() {
        assertFalse(future.isDone(), "Done before resolved");
        future.resolve(5);
        long start = System.currentTimeMillis();
        Integer result = future.get();
        long end = System.currentTimeMillis();
        long duration = end - start;

        // Should be immediate, however we still need to accommodate for the margin of error
        assertTrue(0 <= duration && duration <= 1, "Waited before returning the result");
        assertEquals(5, result, "Different result after resolve");
        assertTrue(future.isDone(), "Not done after resolve");
    }

    @Test
    /*
    Tests that trying to fetch the result blocks the calling thread until future is resolved (using indefinitely blocking get)
     */
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

        long start = System.currentTimeMillis();
        Integer result = future.get();
        long end = System.currentTimeMillis();
        long duration = end - start;

        assertEquals(7, result, "Different result after resolve");
        assertTrue(future.isDone(), "Not done after resolve");
        // error margin of 2ms
        assertTrue(20 <= duration && duration <= 22, "Waited too long or too little to get the result: " + duration + "ms");

        TestUtils.closeThread(resolver);
    }

    @Test
    /*
    Tests that trying to fetch the result after resolving returns immediately (using the timed get)
     */
    public void immediateTimedResolveGet() {
        assertFalse(future.isDone(), "Done before resolved");

        future.resolve(5);
        long start = System.currentTimeMillis();
        Integer result = future.get(50, TimeUnit.MILLISECONDS);
        long end = System.currentTimeMillis();
        long duration = end - start;

        // error margin of 1ms
        assertTrue(0 <= duration && duration <= 1, "Waited before returning the result");
        assertEquals(5, result, "Different result after resolve");
        assertTrue(future.isDone(), "Not done after resolve");
    }

    @Test
    /*
    Tests that trying to fetch the result blocks the calling thread until future is resolved (using the timed get)
    (and that it stops waiting for the result even before the timeout expired if the result is available)
     */
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
        Integer result = future.get(60, TimeUnit.MILLISECONDS);
        long end = System.currentTimeMillis();
        long duration = end - start;

        assertEquals(7, result, "Different result after resolve");
        assertTrue(future.isDone(), "Not done after resolve");
        // error margin of 2ms
        assertTrue(20 <= duration && duration <= 22, "Waited too long or too little to get the result: " + duration + "ms");

        TestUtils.closeThread(resolver);
    }

    @Test
    /*
    Tests that trying to fetch the result times out if the future isn't resolved in time
     */
    public void timedGet_timeout() {
        assertFalse(future.isDone(), "Done before resolved");
        Thread resolver = new Thread(() -> {
            try {
                Thread.sleep(80);
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
        assertFalse(future.isDone(), "Done despite timeout");
        // error margin of 2ms
        assertTrue(30 <= duration && duration <= 32, "Waited too long or too little: " + duration + "ms");

        TestUtils.closeThread(resolver);
    }
}
