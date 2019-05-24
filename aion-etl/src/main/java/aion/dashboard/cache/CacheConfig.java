package aion.dashboard.cache;

public class CacheConfig {
    private int initialSize;
    private int maxSize;
    private int expirationDeadline;

    CacheConfig(int initialSize, int maxSize, int expirationDeadline) {
        this.initialSize = initialSize;
        this.maxSize = maxSize;
        this.expirationDeadline = expirationDeadline;
    }

    CacheConfig() {
        initialSize = 100;
        maxSize = 10_000;
        expirationDeadline = 1;
    }


    public CacheConfig setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public CacheConfig setExpirationDeadline(int expirationDeadline) {
        this.expirationDeadline = expirationDeadline;
        return this;
    }

    public CacheConfig setInitialSize(int initialSize) {
        this.initialSize = initialSize;
        return this;
    }

    public int getExpirationDeadline() {
        return expirationDeadline;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getInitialSize() {
        return initialSize;
    }


}
