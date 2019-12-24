package bgu.spl.mics;

public interface ReadWriteLock {
    void acquireReadLock() throws InterruptedException;
    void releaseReadLock();

    void acquireWriteLock() throws InterruptedException;
    void releaseWriteLock();
}
