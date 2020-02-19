package testing;

import app_kvServer.KVServer;

public class PerfTestHelper {
//    private static IKVServer server;
//    private static int port;
//    private static int cacheSize;
//    private static String strategy;
    public static double putPerc;

//    public PerfTestHelper(int port, int cacheSize, String strategy){
//        this.port = port;
//        this.cacheSize = cacheSize;
//        this.strategy = strategy;
//    }

    public static void perfTest(KVServer server, int num, double putPerc){
        int putIter = (int) Math.round(num*putPerc);
        String[] keys = new String[putIter];
        int getIter = num - putIter;

        long t1 = 0;
        long t2 = 0;
        try {
            for (int i = 0; i < putIter; i++) {
                String key = "foo"+i;
                keys[i] = key;
            }
            t1 = System.currentTimeMillis();

            for (int j = 0; j < putIter; j++) {
                String k1 = keys[(int) (Math.random()*putIter)];
                server.putKV(k1, "barrrrrrrr");
            }
            for (int k = 0; k < getIter; k++) {
                String k2 = keys[(int) (Math.random()*putIter)];
                server.getKV(k2);
            }

            t2 = System.currentTimeMillis();

        } catch (Exception e) {}
        System.out.println("Finished perfTest, strategy: "+server.getCacheStrategy()
                                            +", putPerc: "+putPerc
                                            +", took: "+(t2-t1));
    }

    public static void main(String[] args) {
        if (args.length!=5){
            System.out.println("Invalid argument length");
        }
        else {
            int port = Integer.parseInt(args[0]);
            int cacheSize = Integer.parseInt(args[1]);
            String cacheStrategy = args[2];
            KVServer server = new KVServer(port, cacheSize, cacheStrategy);

            int totNum = Integer.parseInt(args[3]);
            int mode = Integer.parseInt(args[4]);

            switch (mode) {
                case (0):
                    putPerc = 0.8;
                    break;
                case (1):
                    putPerc = 0.5;
                    break;
                case (2):
                    putPerc = 0.2;
                    break;
                default:
                    System.out.println("Wrong mode input");
                    putPerc = 0.8;
            }
            server.clearStorage();
            perfTest(server, totNum, putPerc);
        }
    }


}
