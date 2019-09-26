package aion.dashboard.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class GuavaManager<K,V> extends CacheManager<K, V> {


    private com.google.common.cache.Cache<K,V> intCache;

    GuavaManager(CacheConfig config) {
        super(config);
        init();
    }

    GuavaManager(CacheBuilder builder){
        super(null);
        intCache = builder.build();
    }

    private static final Logger LOGGER_GENERAL = LoggerFactory.getLogger("logger_general");
    private void init(){
        intCache = CacheBuilder.newBuilder()
                .expireAfterAccess(this.config.getExpirationDeadline(), TimeUnit.HOURS)
                .initialCapacity(this.config.getInitialSize())
                .maximumSize(this.config.getMaxSize())
                .recordStats()
                .removalListener(GuavaManager::removalListener)
                .build();
    }

    @Override
    public void putIfAbsent(K key, V value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        if (intCache.getIfPresent(key) == null){
            intCache.put(key,value);
        }

    }

    @Override
    public V getIfPresent(K key) {
        return intCache.getIfPresent(key);
    }


    private static void  removalListener(RemovalNotification removalNotification){
        if (removalNotification.getValue() instanceof Closeable){
            try {
                ((Closeable) removalNotification.getValue()).close();
                if (LOGGER_GENERAL.isTraceEnabled()) {
                    LOGGER_GENERAL.trace("Closing shared IO");
                }
            } catch (Exception ex) {
                LOGGER_GENERAL.error("Caught exception while closing the shared resource: ", ex);
            }
        }
        if (LOGGER_GENERAL.isTraceEnabled()) {
            LOGGER_GENERAL.trace("Evicted key: {}. Caused by: {}", removalNotification.getKey(), removalNotification.getCause());
        }
    }
}
