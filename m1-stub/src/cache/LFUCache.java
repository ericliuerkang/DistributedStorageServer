package cache;

import java.util.*;
//import java.util.Collections;
//import java.util.Map;

public class LFUCache extends ICache{

    protected int tick;
    protected TreeSet<Node> freq_list;

    public LFUCache (int size){
        super(size+1,true);
        tick = 0;
        TreeSet<Node> set = new TreeSet<Node>((a, b) -> a.freq == b.freq ? b.tick - a.tick : b.freq - a.freq);
        freq_list = (TreeSet<Node>) Collections.synchronizedSortedSet(set);
    }


    //Original implementation has fields Node prev, next
    private class Node{
        String key;
        String value;
        int freq = 0;
        int tick = 0;
        Node(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    protected void incrFreq(Node n){
        freq_list.remove(n);
        n.freq ++;
        n.tick = ++tick;
        freq_list.add(n);
    }

    @Override
    protected String readCache(String key) {

        String value =  cache.getOrDefault(key, null);
        if (value != null){
            Node n = new Node(key, value);
            incrFreq(n);
            return value;
        }
        else{
            System.out.println("Key not found in Cache");
            return null;
        }
    }

    @Override
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

        Node n = new Node(key, value);
        incrFreq(n);
    }

    @Override
    protected void evict() {
        Node last = freq_list.pollLast();
        cache.remove(last.key);
        currentCacheSize --;
    }

    @Override
    protected void deleteCache(String key) {
        String value = readCache(key);
        Node n = new Node(key, value);
        freq_list.remove(n);
        cache.remove(key);
    }

}
