package cache;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.Collections;

public class KVCache{

    protected ICache cache;
    private static Logger logger = Logger.getRootLogger();

    public KVCache(String strategy, int size){
        switch (strategy){
            case ("FIFO"):
                cache = new FIFOCache(size);
                logger.info("Initialize " + strategy +
                        " cache with cache size: " + size);
                break;
            case("LRU"):
                cache = new LRUCache(size);
                logger.info("Initialize " + strategy +
                        " cache with cache size: " + size);
                break;
            case("LFU"):
                cache = new LFUCache(size);
                logger.info("Initialize " + strategy +
                        " cache with cache size: " + size);
                break;
            default:
                cache = null;
                logger.info("Unknown strategy");
        }
        if (size <= 0){
            //System.out.println("Invalid Cache size: "+size);
            logger.info("Invalid Cache size");
            cache = null;
        }
    }

    public void putKV(String key, String value){
        synchronized (cache) {
            cache.writeCache(key, value);
            logger.info("Finished put; key: " + key +
                    ", value: " + value);
        }
    }

    public boolean inCache(String key){
        synchronized (cache) {
            return cache.inCache(key);
        }
    }

    public String getV(String key){
        synchronized (cache) {
            if (!cache.inCache(key)) {
                logger.info("Key not in cache; key: "+key);
                return null;
            }
            else {
                logger.info("Finished put; key: " + key);
                return cache.readCache(key);
            }
        }
    }

    public void deleteKV(String key){
        synchronized (cache) {
            if (!cache.inCache(key)) {
                logger.info("Key not in cache; key: "+key);
            }
            else{
                logger.info("Key deleted; key: "+key);
                cache.deleteCache(key);
            }
        }
    }

    public void clearCache(){
        synchronized (cache){
            cache.clearCache();
        }
    }

    public int getCurrentCacheSize() {
        synchronized (cache) {return cache.currentCacheSize;}
    }
    
    public int getMaxCacheSize(){
        synchronized (cache) {return cache.maxCacheSize;}

    }

}
