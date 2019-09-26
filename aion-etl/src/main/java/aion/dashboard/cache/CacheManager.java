package aion.dashboard.cache;

import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.function.Supplier;

public abstract class CacheManager<K ,V> {


    public enum Cache{
        CONTRACT,
        TOKEN,
        ATS_TOKEN,
        CONTRACT_EXISTS,
        CIRCULATING_SUPPLY
    }

    private static final EnumMap<Cache, CacheManager> CACHEMAP = new EnumMap<>(Cache.class);
    protected final CacheConfig config;


    protected CacheManager(CacheConfig config){

        this.config = config;
    }

    public static<V> CacheManager<String ,V> getManager(Cache cache){
        //noinspection unchecked
        return  (GuavaManager<String, V>)CACHEMAP.computeIfAbsent(cache, cache1 -> new GuavaManager<String, V>(new CacheConfig()));
    }

    public static <K,V> CacheManager<K,V> getExpiringCache(Cache cache, Duration duration, int size){
        return CACHEMAP.computeIfAbsent(cache,
                cache1 ->
                        new GuavaManager(CacheBuilder.newBuilder().expireAfterWrite(duration).maximumSize(size)));
    }


    public abstract void putIfAbsent(K key, V value);

    public abstract V getIfPresent(K key);

    public final boolean contains(K key){
        return getIfPresent(key) != null;
    }

    public final V computeIfAbsent(K key, Supplier<V> vSupplier){
        V vValue =vSupplier.get();
        putIfAbsent(key, vValue);
        return vValue;
    }
}
