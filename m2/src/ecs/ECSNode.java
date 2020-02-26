package ecs;

public class ECSNode<T extends IECSNode> implements  IECSNode {
    final T physicalNode;
    final int replicaIndex;

    public enum ECSNodeFlag{
        STOP,
        START,
        STATE_CHANGE,
        KV_TRANSFER,
        SHUT_DOWN,
        UPDATE,
        TRANSFER_FINISH
    }

    public ECSNode(T physicalNode, int replicaIndex) {
        this.physicalNode = physicalNode;
        this.replicaIndex = replicaIndex;
    }

    @Override
    public String getNodeName() {
        return null;
    }

    @Override
    public String getNodeHost() {
        return null;
    }

    @Override
    public int getNodePort() {
        return 0;
    }

    @Override
    public String[] getNodeHashRange() {
        return new String[0];
    }
}
