package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 *
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
    private T result;
    private boolean isDone;

    /**
     * This should be the the only public constructor in this class.
     */
    public Future() {
        result = null;
        isDone = false;
    }

    /**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     *
     * @return return the result of type T if it is available, if not wait until it is available.
     */
    public T get() throws InterruptedException {
        if (isDone) {
            return result;
        }

        return waitAndReturn();
    }

    /**
     * Resolves the result of this Future object.
     */
    public void resolve(T result) {
        synchronized (this) {
            this.result = result;
            isDone = true;
            stopBlocking();
        }
    }

    /**
     * @return true if this object has been resolved, false otherwise
     */
    public boolean isDone() {
        synchronized (this) {
            return isDone;
        }
    }

    /**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     *
     * @param timeout the maximal amount of time units to wait for the result.
     * @param unit    the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not,
     * wait for {@code timeout} TimeUnits {@code unit}. If time has
     * elapsed, return null.
     */
    public T get(long timeout, TimeUnit unit) throws InterruptedException {
        if (isDone) {
            return result;
        }

        timeout = TimeUnit.NANOSECONDS.convert(timeout, unit);
        return waitUntilTimeoutOrDoneAndReturn(timeout);
    }

    private void stopBlocking() {
        notifyAll();
    }

    private T waitAndReturn() throws InterruptedException {
        synchronized (this) {
            while (shouldWait()) {
                wait();
            }

            return this.result;
        }
    }

    private T waitUntilTimeoutOrDoneAndReturn(long timeout) throws InterruptedException {
        TimeUnit unit = TimeUnit.NANOSECONDS;
        T result = null;
        synchronized (this) {
            while (shouldWait() && timeout > 0) {
                timeout = timedWait(timeout, unit);
            }

            if (isDone) {
                result = this.result;
            }

            return result;
        }
    }

    private long timedWait(long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        unit.timedWait(this, timeout);
        timeout -= (System.nanoTime() - startTime);
        return timeout;
    }

    private boolean shouldWait() {
        return !isDone;
    }
}
