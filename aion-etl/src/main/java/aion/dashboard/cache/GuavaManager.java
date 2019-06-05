package aion.dashboard.cache;

import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class GuavaManager<K,V> extends CacheManager<K, V> {


    private com.google.common.cache.Cache<K,V> intCache;

    GuavaManager(CacheConfig config) {
        super(config);
        init();
    }

    private static final Logger LOGGER_GENERAL = LoggerFactory.getLogger("logger_general");
    private void init(){
        intCache = CacheBuilder.newBuilder()
                .expireAfterAccess(this.config.getExpirationDeadline(), TimeUnit.HOURS)
                .initialCapacity(this.config.getInitialSize())
                .maximumSize(this.config.getMaxSize())
                .recordStats()
                .removalListener(e -> LOGGER_GENERAL.debug("Evicted key: {}. Caused by: {}",e.getKey(), e.getCause()))
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


}
