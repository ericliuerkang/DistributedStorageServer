package testing;

import cache.KVCache;
import client.KVStore;
import org.junit.Test;

import junit.framework.TestCase;
import shared.messages.KVMessage;

public class AdditionalTest extends TestCase {

   private KVCache FIFO;
   private KVCache LRU;
   private KVCache LFU;
   
   // TODO add your test cases, at least 3
   
   @Test
   public void FIFOTest() {
      FIFO = new KVCache("FIFO", 2);

      FIFO.putKV("foo1","bar1");
      System.out.println(FIFO.toString());
      FIFO.putKV("foo2","bar2");
      System.out.println(FIFO.toString());
      String tmp = LRU.getV("foo1");
      FIFO.putKV("foo3","bar3");
      System.out.println(FIFO.toString());

      String val1 = FIFO.getV("foo1");
      String val2 = FIFO.getV("foo2");
      String val3 = FIFO.getV("foo3");

      assertSame(null, val1);
      assertSame("bar2", val2);
      assertSame("bar3", val3);
   }

   @Test
   public void LRUTest() {
      LRU = new KVCache("LRU", 2);

      LRU.putKV("foo1","bar1");
      System.out.println(LRU.toString());
      LRU.putKV("foo2","bar2");
      System.out.println(LRU.toString());
      String tmp = LRU.getV("foo1");
      LRU.putKV("foo3","bar3");
      System.out.println(LRU.toString());

      String val1 = LRU.getV("foo1");
      String val2 = LRU.getV("foo2");
      String val3 = LRU.getV("foo3");

      assertSame("bar1", val1);
      assertSame(null, val2);
      assertSame("bar3", val3);
   }

   @Test
   public void LFUTest() {
      LFU = new KVCache("LFU", 2);

      LFU.putKV("foo1","bar1");
      System.out.println(LFU.toString());
      String tmp1 = LRU.getV("foo1");
      String tmp2 = LRU.getV("foo1");
      LFU.putKV("foo2","bar2");
      String tmp3 = LRU.getV("foo2");
      System.out.println(LFU.toString());
      LFU.putKV("foo3","bar3");
      String tmp4 = LRU.getV("foo3");
      String tmp5 = LRU.getV("foo3");
      System.out.println(LFU.toString());
      LFU.putKV("foo4","bar4");
      System.out.println(LFU.toString());


      String val1 = LFU.getV("foo1");
      String val2 = LFU.getV("foo2");
      String val3 = LFU.getV("foo3");
      String val4 = LFU.getV("foo4");

      assertSame("bar1", val1);
      assertSame(null, val2);
      assertSame("bar3", val3);
      assertSame("bar4", val4);
   }
}