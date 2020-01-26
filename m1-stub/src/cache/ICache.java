package cache;
import java.util.*;
//import java.util.LinkedHashMap;
//import java.util.Map;

public abstract class ICache{
    int maxCacheSize;
    int currentCacheSize;
    Map<String, String> cache;

    public ICache(int size, boolean accessOrder){
        System.out.println("ICache size: "+size);
        this.maxCacheSize = size;
        this.currentCacheSize = 0;
        this.cache = (Map<String, String>) Collections.synchronizedMap(
                        new LinkedHashMap<String, String>(this.maxCacheSize, 0.75f, accessOrder));
    }

    protected String readCache(String key) {
        String value = null;
        if (cache.containsKey(key)){
            return cache.get(key);
        }
        else{ return null; }
    }

    protected void writeCache(String key, String value) {
        if (!cache.containsKey(key)) {
//            System.out.println("curr: "+currentCacheSize+"max: "+maxCacheSize);
            if (currentCacheSize == maxCacheSize){
                evict();
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
        if (cache.containsKey(key)){
            cache.remove(key);
            currentCacheSize --;
        }
    }

    protected boolean inCache(String key) {
        return cache.containsKey(key);
    }

    protected void clearCache() {
        cache.clear();
        currentCacheSize = 0;
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