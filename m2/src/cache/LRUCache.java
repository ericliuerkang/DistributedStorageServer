package cache;

import java.util.LinkedHashMap;
//import java.util.Collections;
import java.util.Map;


public class LRUCache extends ICache {

    public LRUCache (int size){
        super(size, true);
    }

//    @Override
//    protected String readCache(String key) {
//        if (cache.containsKey(key)){
//            System.out.println("yes");
//            String value = cache.get(key);
//            cache.remove(key);
//            cache.put(key, value);
//            return value;
//        }
//        else{ return null; }
//    }
//
//    @Override
//    protected void writeCache(String key, String value) {
//        if (!cache.containsKey(key)) {
//            if (currentCacheSize == maxCacheSize){
//                evict();
//                currentCacheSize --;
//            }
//            cache.put(key, value);
//            currentCacheSize ++;
//        }
//        else {
//            cache.remove(key);
//            cache.put(key, value);
//        }
//    }

//    @Override
//    protected String readCache(String key) {
//        String value =  getOrDefault(key, null);
//        if (value != null){
//            return value;
//        }
//        else{
//            System.out.println("Key not found in Cache");
//            return null;
//        }
//    }
//
//    @Override
//    protected void writeCache(String key, String value) {
//        super.put(key, value);
//    }
//
//    @Override
//    protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
//        return super.size() > maxCacheSize;
//    }
//
//    @Override
//    protected void deleteCache(String key) {
//        super.remove(key);
//    }
//
//    @Override
//    protected boolean inCache(String key) {
//        return super.containsKey(key);
//    }
//
//    @Override
//    protected void clearCache() {
//        super.clear();
//    }
//
//    @Override
//    public int getMaxCacheSize() {
//        return maxCacheSize;
//    }
//
//    @Override
//    public int getCurrentCacheSize() {
//        return currentCacheSize;
//    }
//
//    @Override
//    public void printContent() {
//        System.out.println(super.entrySet());
//    }
}
