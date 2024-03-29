package bgu.spl.mics;

public interface ReadWriteLock {
    void acquireReadLock();
    void releaseReadLock();

    void acquireWriteLock();
    void releaseWriteLock();
}
