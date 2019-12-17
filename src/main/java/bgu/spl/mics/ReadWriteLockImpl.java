package bgu.spl.mics;

public class ReadWriteLockImpl implements ReadWriteLock {
    protected int activeReaders = 0;  // threads executing read
    protected int activeWriters = 0;  // always zero or one
    protected int waitingReaders = 0; // threads not yet in read_
    protected int waitingWriters = 0; // same for write

    public void acquireReadLock() {
        beforeRead();
    }

    public void releaseReadLock() {
        afterRead();
    }

    public void acquireWriteLock() {
        beforeWrite();
    }

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
