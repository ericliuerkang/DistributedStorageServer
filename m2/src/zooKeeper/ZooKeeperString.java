package zooKeeper;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ZooKeeperString {
    public static String ZK_HOST = null;
    static {
        try {
            ZK_HOST = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            ZK_HOST = "127.0.0.1";
        }
    }
    public static final String ZK_PORT = "2181";
    public static final String ZK_CONN = ZK_HOST + ":" + ZK_PORT;
    // ZooKeeper connection timeout in millisecond
    public static final int ZK_TIMEOUT = 2000;
    public static final String ZK_SERVER_ROOT = "/kv_servers";
    public static final String ZK_ACTIVE_ROOT = "/active";
    public static final String ZK_METADATA_ROOT = "/metadata";
}
