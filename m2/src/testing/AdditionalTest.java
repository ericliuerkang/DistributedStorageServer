package testing;

import cache.KVCache;
import client.KVStore;
import junit.framework.TestCase;
import org.apache.log4j.Level;
import org.junit.Test;
import shared.messages.KVMessage;

public class AdditionalTest extends TestCase {

   private KVStore kvClient;

   public void setUp() {
      kvClient = new KVStore("localhost", 5000 );
      try {
         kvClient.connect();
      } catch(Exception e) { }}


   @Test
   public void testFIFO() {
      KVCache FIFO = new KVCache("FIFO", 2);

      FIFO.putKV("foo1","bar1");
      FIFO.putKV("foo2","bar2");
      String tmp = FIFO.getV("foo1");
      FIFO.putKV("foo3","bar3");

      String val1 = FIFO.getV("foo1");
      String val2 = FIFO.getV("foo2");
      String val3 = FIFO.getV("foo3");

      assertSame(null, val1);
      assertSame("bar2", val2);
      assertSame("bar3", val3);
   }

   @Test
   public void testLRU() {
      KVCache LRU = new KVCache("LRU", 2);

      LRU.putKV("foo1","bar1");
      LRU.putKV("foo2","bar2");
      String tmp = LRU.getV("foo1");
      LRU.putKV("foo3","bar3");

      String val1 = LRU.getV("foo1");
      String val2 = LRU.getV("foo2");
      String val3 = LRU.getV("foo3");

      assertSame("bar1", val1);
      assertSame(null, val2);
      assertSame("bar3", val3);
   }

   @Test
   public void testLFU() {
      KVCache LFU = new KVCache("LFU", 2);

      LFU.putKV("foo1","bar1");
      String tmp1 = LFU.getV("foo1");
      String tmp2 = LFU.getV("foo1");
      tmp2 = LFU.getV("foo1");
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

      assertSame("bar1", val1);
      assertSame(null, val2);
      assertSame(null, val3);
      assertSame("bar4", val4);
   }

   @Test
   public void testClearCache() {
      Exception ex = null;
      int s3 = -1;
      KVCache C1 = new KVCache("FIFO", 0);
      KVCache C2 = new KVCache("LRU", 2);
      KVCache C3 = new KVCache("JIAZESB", 5);

      C1.clearCache();
      C2.clearCache();
      C3.clearCache();

      int s1 = C1.getMaxCacheSize();
      int s2 = C2.getMaxCacheSize();
      try{
         s3 = C3.getMaxCacheSize();
      }
      catch (Exception e){
         ex = e;
      }
      assertSame(0,s1);
      assertSame(2,s2);
      assertTrue(ex==null && s3==5);
   }

   @Test
   public void testInCache(){
      Exception ex = null;
      KVCache C1 = new KVCache("FIFO", 0);
      KVCache C2 = new KVCache("LRU", 2);
      KVCache C3 = new KVCache("JIAZESB", 5);

      C2.putKV("foo2","bar2");
      assertTrue(C2.inCache("foo2"));
      assertFalse(C2.inCache("foo1"));
      C2.clearCache();
      assertFalse(C2.inCache("foo2"));

      C3.putKV("foo3","bar3");
      assertTrue(C3.inCache("foo3"));
      assertFalse(C3.inCache("foo1"));
      C3.clearCache();
      assertFalse(C3.inCache("foo3"));
   }

   @Test
   public void testPutKeyTooLong() {
      String key = "fooofooofooofooofoooff";
      String value = "bar2";
      KVMessage response = null;
      Exception ex = null;

      try {
         response = kvClient.put(key, value);
      } catch (Exception e) {
         ex = e;
         ex.printStackTrace();
      }

      assertTrue(ex == null &&response.getStatus() == KVMessage.StatusType.PUT_ERROR);
   }
   
   @Test
   public void testDeleteKeyTooLong() {
      String key = "fooofooofooofooofoooff";
      String value = null;
      KVMessage response = null;
      Exception ex = null;

      try {
         response = kvClient.put(key, value);
      } catch (Exception e) {
         ex = e;
         ex.printStackTrace();
      }

      assertTrue(ex == null &&response.getStatus() == KVMessage.StatusType.DELETE_ERROR);
   }

   @Test
   public void testPutValueTooLong() {
      StringBuilder sb =
              new StringBuilder(200000);
      for(int i=0; i < 200000; i++){
         sb.append('a');
      }
      String key = "foo233";
      String value = sb.toString();
      KVMessage response = null;
      Exception ex = null;

      try {
         response = kvClient.put(key, value);
      } catch (Exception e) {
         ex = e;
         ex.printStackTrace();
      }

      assertTrue(ex == null && response.getStatus() == KVMessage.StatusType.PUT_ERROR);
   }

   @Test
   public void testPutBadKey() {
      String value = "bar666";
      String key1 = "";
      String key2 = " ";
      String key3 = null;
      KVMessage response1 = null;
      KVMessage response2 = null;
      KVMessage response3 = null;
      Exception ex = null;

      try {
         response1 = kvClient.put(key1, value);
         response2 = kvClient.put(key2, value);
         response3 = kvClient.put(key3, value);
      } catch (Exception e) {
         ex = e;
      }
      assertTrue( response1.getStatus() == KVMessage.StatusType.PUT_ERROR
              && response2.getStatus() == KVMessage.StatusType.PUT_ERROR
              && response3.getStatus() == KVMessage.StatusType.PUT_ERROR);
   }
   @Test
   public void testDeleteBadKey() {
      String value = null;
      String key1 = "";
      String key2 = " ";
      String key3 = null;
      KVMessage response1 = null;
      KVMessage response2 = null;
      KVMessage response3 = null;
      Exception ex = null;

      try {
         response1 = kvClient.put(key1, value);
         response2 = kvClient.put(key2, value);
         response3 = kvClient.put(key3, value);
      } catch (Exception e) {
         ex = e;
      }

      assertTrue(ex == null && response1.getStatus() == KVMessage.StatusType.DELETE_ERROR
              && response2.getStatus() == KVMessage.StatusType.DELETE_ERROR
              && response3.getStatus() == KVMessage.StatusType.DELETE_ERROR
              );
   }
//
//   @Test
//   public void testLogLevel() {
//      String level = null;
//      Exception ex = null;
//      try {
//         level = kvClient.setLevel(Level.DEBUG.toString());
//      } catch (Exception e) {
//         ex = e;
//         ex.printStackTrace();
//      }
//      assertTrue(ex == null && level.equals(Level.DEBUG) == true);
//   }
}