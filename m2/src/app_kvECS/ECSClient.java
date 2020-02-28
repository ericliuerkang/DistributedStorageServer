package app_kvECS;

import java.util.Map;
import java.util.Collection;

import ecs.ECSNode;
import org.apache.log4j.Logger;

import ecs.IECSNode;
import shared.dataTypes.MetaData;


public class ECSClient implements IECSClient {
    private MetaData metaData;
    private static Logger logger = Logger.getRootLogger();
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

    @Override
    public IECSNode addNode(String cacheStrategy, int cacheSize) {
        // TODO
        return null;
    }

    @Override
    public Collection<IECSNode> addNodes(int count, String cacheStrategy, int cacheSize) {
        // TODO
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
        return null;
    }

    public static void main(String[] args) {
        // TODO
    }
}
