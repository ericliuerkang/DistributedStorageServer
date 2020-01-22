package cache;
import java.util.*;
import java.util.Collections;

public class KVCache{

    protected ICache cache;

    public KVCache(String strategy, int size){
        switch (strategy){
            case ("FIFO"):
                cache = new FIFOCache(size);
                break;
            case("LRU"):
                cache = new LRUCache(size);
                break;
            case("LFU"):
                cache = new LFUCache(size);
                break;
        }
    }

    public void putKV(String key, String value){
        synchronized (cache) {
            cache.writeCache(key, value);
        }
    }

    public boolean inCache(String key){
        synchronized (cache) {
            return cache.inCache(key);
        }
    }

    public String getV(String key){
        synchronized (cache) {
            return cache.readCache(key);
        }
    }

    public void clearCache(){
        cache.clearCache();
    }

    public int getCurrentCacheSize() {
        synchronized (cache) {return cache.currentCacheSize;}
    }

}
