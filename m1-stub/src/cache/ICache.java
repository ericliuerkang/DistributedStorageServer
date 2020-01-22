package cache;
import java.util.*;
//import java.util.LinkedHashMap;
//import java.util.Map;

public abstract class ICache{
    int maxCacheSize;
    int currentCacheSize;
    Map<String, String> cache;

    public ICache(int size, boolean accessOrder){
        this.maxCacheSize = size;
        this.currentCacheSize = 0;
        this.cache = (Map<String, String>) Collections.synchronizedMap(
                        new LinkedHashMap<String, String>(size, 0.75f, accessOrder));
    }

    protected String readCache(String key) {
        String value =  cache.getOrDefault(key, null);
        if (value == null){
            System.out.println("Key not found in Cache");
        }
        return value;
    }

    protected void writeCache(String key, String value) {
        if (!cache.containsKey(key)) {
            if (currentCacheSize == maxCacheSize){
                evict();
                currentCacheSize --;
            }
            cache.put(key, value);
            currentCacheSize ++;
        }
        else cache.replace(key, value);
    }

    protected void evict() {
        cache.remove(cache.keySet().iterator().next());
        currentCacheSize --;
    }

    protected void deleteCache(String key) {
        cache.remove(key);
        currentCacheSize --;
    }

    protected boolean inCache(String key) {
        return cache.containsKey(key);
    }

    protected void clearCache() {
        cache.clear();
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public int getCurrentCacheSize() {
        return currentCacheSize;
    }

    public void printContent() {
        System.out.println(cache.entrySet());
    }
}