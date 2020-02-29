package app_kvECS;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import app_kvServer.KVServer;
import ecs.ECSNode;
import ecs.HashRing;
import ecs.MetaRing;
import org.apache.log4j.Logger;

import ecs.IECSNode;
import org.apache.zookeeper.*;
import shared.dataTypes.MetaData;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class ECSClient implements IECSClient {
    private final ZooKeeper zk;
    private int zkPort = 1234;
    private String zkHost = "localhost";

    private TreeMap<BigInteger, MetaData> metaData;
    private static Logger logger = Logger.getRootLogger();
    private HashRing<ECSNode> hashRing;
    private MetaRing<MetaData> metaRing;
    private ArrayList<String> serverFile;
    private static String jarPath;

    public ECSClient() throws IOException, InterruptedException {
        String serverScript = System.getProperty("user.dir") + "/" + "server.sh";
        jarPath = new File(serverScript).toString();

        hashRing = new HashRing<ECSNode>();
        metaRing = new MetaRing<MetaData>();
        Path configFile;
        try {
            serverFile = new ArrayList<String>(Files.readAllLines(Paths.get("ecs.config"), StandardCharsets.UTF_8));
        }
        catch (IOException e){logger.info("IOError with ecs.config");}

        CountDownLatch connectionLatch = new CountDownLatch(1);
        this.zk = new ZooKeeper("localhost", 2000, new Watcher() {
            public void process(WatchedEvent we) {
                if (we.getState() == Event.KeeperState.SyncConnected) {
                    connectionLatch.countDown();
                }
            }
        });
        connectionLatch.await();
    }

    @Override
    public boolean start() {
        try {
            for (Map.Entry<BigInteger, IECSNode> entry : hashRing.ring.entrySet()) {
                IECSNode node = entry.getValue();
                node.setNodeStatus(ECSNode.ECSNodeFlag.START);
                zk.setData('/'+node.getNodeName(), node.toBytes(), -1);
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to start ECSClient because: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean stop() {
        try {
            for (Map.Entry<BigInteger, IECSNode> entry : hashRing.ring.entrySet()) {
                IECSNode node = entry.getValue();
                node.setNodeStatus(ECSNode.ECSNodeFlag.STOP);
                zk.setData('/' + node.getNodeName(), node.toBytes(), -1);
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to start ECSClient because: " + e.getMessage());
            return false;
        }
    }
    @Override
    public boolean shutdown() {
        try {
            for (Map.Entry<BigInteger, IECSNode> entry : hashRing.ring.entrySet()) {
                IECSNode node = entry.getValue();
                node.setNodeStatus(ECSNode.ECSNodeFlag.SHUT_DOWN);
                zk.setData('/' + node.getNodeName(), node.toBytes(), -1);
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to start ECSClient because: " + e.getMessage());
            return false;
        }
    }

//    If there are idle servers in the repository (“i.e., in the listed provided in “ecs.config”), randomly pick one of them and send an SSH call to invoke the KVServer process.
//    Determine the position of the new storage server within the ring by hashing its address.
//    Recalculate and update the metadata of the storage service (i.e,, the ranges for the new storage server and its successor)
//    Initialize the new storage server with the updated metadata and start it.
//    Set write lock (lockWrite()) on the successor server;
//    Invoke the transfer of the affected data items (i.e., the range of keys that was previously handled by the successor storage server) to the new storage server. The data that is transferred should not be deleted immediately to be able to serve read requests in the meantime at the successor while transfer is in progress (while the newly added storage server is “write-locked”.)
//successor.moveData(range, newServer)
//    When all affected data has been transferred (i.e., the successor notified the ECS)
//    Send a metadata update to all storage servers (to inform them about their new responsibilities)
//    Release the write lock on the successor server and finally remove the data items that are no longer handled by this server


    @Override
    public IECSNode addNode(String cacheStrategy, int cacheSize){
        //pick server from ecs.config
        Collections.shuffle(serverFile);
        ECSNode node = null;
        String info;
        boolean isAdded = false;
        int port = 0;
        for (String line : serverFile){
            String[] words = line.split(" ");
            if (words.length != 3) {
                logger.info("Wrong ecs.config, line width"+words.length);
                break;
            }
            port = Integer.parseInt(words[2]);
            node = new ECSNode(words[0],words[1],port);
            if (node.nodeStatus == ECSNode.ECSNodeFlag.IDLE){
                node.nodeStatus = ECSNode.ECSNodeFlag.STOP;
                try {
                    hashRing.addNode(node);
                    isAdded = true;}
                catch (NoSuchAlgorithmException e) {logger.info("NoSuchAlgorithmException with addNode");}
                break;
            }
        }
        assert (node != null && isAdded);
        sshStartServer(node);

        //TODO
        try {
            BigInteger hashValue = HashRing.calculateHashValue(node.getNodeHost());
            BigInteger start = hashValue.add(new BigInteger("1"));
            BigInteger end = metaRing.getNextHash(node);
            MetaData md = new MetaData(node.getNodeName(), node.getNodeHost(), node.getNodePort(), start, end);
            metaRing.addNode(md);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
//        metaData.update();
//        for (Map.Entry<BigInteger, IECSNode> enode:hashRing.ring.entrySet()){
//            enode.getValue().setMetaData(metaData);
//        }


        //TODO
        //instantiate a server corresponding to node
        //TODO
        //Transfer data

        return null;
    }

    public void sshStartServer(ECSNode n){
        assert n != null;
        // Issue the ssh call to start the process remotely
        String javaCmd = String.join(" ",
                "java -jar",
                jarPath,
                String.valueOf(n.getNodePort()),
                n.getNodeName(),
                zkHost,
                Integer.toString(zkPort));
        String sshCmd = "ssh -o StrictHostKeyChecking=no -n " + n.getNodeHost() + " nohup " + javaCmd +
                " > server.log &";
        // Redirect output to files so that ssh channel will not wait for further output
        try {
            logger.info("Executing command: " + sshCmd);
            Process p = Runtime.getRuntime().exec(sshCmd);
            Thread.sleep(100);
            // p.waitFor();
            // assert !p.isAlive();
            // assert p.exitValue() == 0;
        } catch (IOException e) {
            logger.error("Unable to launch server with ssh (" + n + ")", e);
            e.printStackTrace();
            try {
                hashRing.removeNode(n); // Connection failed, remove instance from result collection
            } catch (NoSuchAlgorithmException ex1) {
                logger.error(ex1);
            }
        } catch (InterruptedException e) {
            logger.error("Receive an interrupt", e);
            e.printStackTrace();
            try {
                hashRing.removeNode(n); // Connection failed, remove instance from result collection
            } catch (NoSuchAlgorithmException ex2) {
                logger.error(ex2);
            }
        }
    }

    public static void updateMetaData(TreeMap<BigInteger, MetaData> treeMap, MetaData newMetaData){
        for (Map.Entry<BigInteger,MetaData> entry : treeMap.entrySet()){
            entry.setValue(newMetaData);
        }
    }

    @Override
    public Collection<IECSNode> addNodes(int count, String cacheStrategy, int cacheSize) {
        for (int i = 0;i<count;i++){
            addNode(cacheStrategy, cacheSize);
        }
        return null;
    }

    @Override
    public Collection<IECSNode> setupNodes(int count, String cacheStrategy, int cacheSize) {
        // TODO
        return null;
    }

    @Override
    public boolean awaitNodes(int count, int timeout) throws Exception {
        // TODO
        return false;
    }

    @Override
    public boolean removeNodes(Collection<String> nodeNames) {
        // TODO
        return false;
    }

    @Override
    public Map<String, IECSNode> getNodes() {
        return hashRing.getNodes();
    }

    @Override
    public IECSNode getNodeByKey(String Key) {
        try {
            BigInteger hashedKey = HashRing.calculateHashValue(Key);
            return hashRing.ring.get(hashedKey);
        }
        catch (NoSuchAlgorithmException e) {
            logger.error(e);
            return null;
        }
    }

    public static void main(String[] args) {
        // TODO
    }
}
