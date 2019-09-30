package aion.dashboard.db;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SharedDBLocks {

    private static final SharedDBLocks instance = new SharedDBLocks();

    public static SharedDBLocks getInstance() {
        return instance;
    }

    private final ReentrantReadWriteLock dbLock = new ReentrantReadWriteLock();

    public void lockReorg() {
        dbLock.writeLock().lock();
    }

    public void lockDBWrite() {
        dbLock.readLock().lock();
    }

    public void unlockReorg() {
        dbLock.writeLock().unlock();
    }

    public void unlockDBWrite() {
        dbLock.readLock().unlock();
    }
}
