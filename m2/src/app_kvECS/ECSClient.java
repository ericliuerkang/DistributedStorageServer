package app_kvECS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Collection;

import app_kvServer.KVServer;
import ecs.ECSNode;
import ecs.HashRing;
import org.apache.log4j.Logger;

import ecs.IECSNode;
import org.apache.zookeeper.*;
import shared.dataTypes.MetaData;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class ECSClient implements IECSClient {
    private final ZooKeeper zk;
    private MetaData metaData;
    private static Logger logger = Logger.getRootLogger();
    private HashRing<ECSNode> hashRing;
    private ArrayList<String> serverFile;

    public ECSClient() throws IOException, InterruptedException {
        hashRing = new HashRing<ECSNode>();
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
            for (ECSNode node : metaData.ecsNodes) {
                node.nodeStatus = ECSNode.ECSNodeFlag.START;
            }
        } catch (Exception e) {
            logger.error("Failed to start ECSClient because: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean stop() {
        try {
            for (ECSNode node : metaData.ecsNodes) {
                node.nodeStatus = ECSNode.ECSNodeFlag.STOP;
            }
        } catch (Exception e) {
            logger.error("Failed to stop ECSClient because: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean shutdown() {
        try {
            for (ECSNode node : metaData.ecsNodes) {
                node.nodeStatus = ECSNode.ECSNodeFlag.SHUT_DOWN;
            }
        } catch (Exception e) {
            logger.error("Failed to shut down ECSClient because: " + e.getMessage());
            return false;
        }
        return true;
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
                try {hashRing.addNode(node);}
                catch (NoSuchAlgorithmException e) {logger.info("NoSuchAlgorithmException with addNode");}
                break;
            }
        }

        //TODO
//        MetaData.update();

        try {
            zk.create('/' + node.getNodeName(), node.toBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        catch (NullPointerException | KeeperException | InterruptedException e) {logger.error(e);}

        //TODO
        //instantiate a server correponding to node
        //TODO
        //Transfer data

        return null;
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
        // TODO
        return null;
    }

    @Override
    public IECSNode getNodeByKey(String Key) {
        // TODO
        return hashRing.ring.get(Key);
    }

    public static void main(String[] args) {
        // TODO
    }
}
