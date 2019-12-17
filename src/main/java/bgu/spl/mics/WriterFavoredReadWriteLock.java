package bgu.spl.mics;

/**
 * A read/write lock which favors writers, very similar to the one seen in class.
 */
public class WriterFavoredReadWriteLock implements ReadWriteLock {
    protected int activeReaders = 0;  // threads executing read
    protected int activeWriters = 0;  // always zero or one
    protected int waitingReaders = 0; // threads not yet in read
    protected int waitingWriters = 0; // same for write

    /**
     * Acquires a read lock. This method is blocking until it is possible to acquire the lock.
     */
    public void acquireReadLock() {
        beforeRead();
    }

    /**
     * Releases a read lock.
     */
    public void releaseReadLock() {
        afterRead();
    }

    /**
     * Acquires a write lock. This method is blocking until it is possible to acquire the lock.
     */
    public void acquireWriteLock() {
        beforeWrite();
    }

    /**
     * Releases a write lock.
     */
    public void releaseWriteLock() {
        afterWrite();
    }

    private boolean allowReader() {
        return waitingWriters == 0 && activeWriters == 0;
    }

    private boolean allowWriter() {
        return activeReaders == 0 && activeWriters == 0;
    }

    private synchronized void beforeRead() {
        ++waitingReaders;
        while (!allowReader())
            try { wait(); } catch (InterruptedException ignored) {}
        --waitingReaders;
        ++activeReaders;
    }

    private synchronized void afterRead()  {
        --activeReaders;
        notifyAll();  // Will unblock any pending writer
    }

    private synchronized void beforeWrite() {
        ++waitingWriters;
        while (!allowWriter())
            try { wait(); } catch (InterruptedException ignored) {}
        --waitingWriters;
        ++activeWriters;
    }

    private synchronized void afterWrite() {
        --activeWriters;
        notifyAll(); // Will unblock waiting writers and waiting readers
    }
}
