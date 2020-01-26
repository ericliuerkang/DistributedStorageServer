package cache;

import app_kvServer.KVServer;
import logger.LogSetup;
import org.apache.log4j.Level;
import org.junit.Test;

import java.io.IOException;

public class testCache {

    public static void FIFOTest() {

        KVCache FIFO = new KVCache("FIFO", 2);

        System.out.println("size: "+FIFO.cache.maxCacheSize);

        FIFO.putKV("foo1","bar1");
        FIFO.putKV("foo2","bar2");
        String tmp = FIFO.getV("foo1");
        FIFO.putKV("foo3","bar3");

        String val1 = FIFO.getV("foo1");
        String val2 = FIFO.getV("foo2");
        String val3 = FIFO.getV("foo3");

        FIFO.clearCache();
        String val4 = FIFO.getV("foo1");
        String val5 = FIFO.getV("foo2");
        String val6 = FIFO.getV("foo3");

        System.out.println(val1+", "+val2+", "+val3+", "+val4+", "+val5+", "+val6);
    }

    public static void LRUTest() {
        KVCache LRU = new KVCache("LRU", 2);

        LRU.putKV("foo1","bar1");
        //LRU.putKV("foo1","tmp");
        LRU.putKV("foo2","bar2");
        String tmp = LRU.getV("foo1");
        LRU.putKV("foo3","bar3");
        LRU.putKV("foo4","bar4");
        System.out.println("currsize: "+LRU.cache.currentCacheSize);
        System.out.println("psize: "+LRU.cache.cache.size());

        String val1 = LRU.getV("foo1");
        String val2 = LRU.getV("foo2");
        String val3 = LRU.getV("foo3");
        String val4 = LRU.getV("foo4");

        LRU.clearCache();
//        System.out.println("size: "+LRU.cache.maxCacheSize);

        String val5 = LRU.getV("foo1");
        String val6 = LRU.getV("foo2");
        String val7 = LRU.getV("foo3");

        System.out.println(val1+", "+val2+", "+val3+", "+val4+", "+val5+", "+val6+", "+val7);
    }

    public static void LFUTest() {
        KVCache LFU = new KVCache("LFU", 2);

        LFU.putKV("foo1","bar1");
        String tmp1 = LFU.getV("foo1");
        String tmp2 = LFU.getV("foo1");
        LFU.putKV("foo2","bar2");
        String tmp3 = LFU.getV("foo2");
        LFU.putKV("foo3","bar3");
        String tmp4 = LFU.getV("foo3");
        String tmp5 = LFU.getV("foo3");
        LFU.putKV("foo4","bar4");


        String val1 = LFU.getV("foo1");
        String val2 = LFU.getV("foo2");
        String val3 = LFU.getV("foo3");
        String val4 = LFU.getV("foo4");

        System.out.println(val1+", "+val2+", "+val3+", "+val4);
    }

    public static void main(String[] args) {
        FIFOTest();
//        LRUTest();
//        LFUTest();
    }
}
