package aion.dashboard.cache;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class CacheManager<K ,V> {


    public enum Cache{
        CONTRACT,TOKEN;

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




    public abstract void putIfAbsent(K key, V value);

    public abstract V getIfPresent(K key);

    public final boolean contains(K key){
        return getIfPresent(key) == null;
    }

    public final V computeIfAbsent(K key, Supplier<V> vSupplier){
        V vValue =vSupplier.get();
        putIfAbsent(key, vValue);
        return vValue;
    }
}
