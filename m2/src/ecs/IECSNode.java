package ecs;

import shared.dataTypes.MetaData;

import java.math.BigInteger;
import java.util.TreeMap;

public interface IECSNode {

    /**
     * @return  the name of the node (ie "Server 8.8.8.8")
     */
    public String getNodeName();

    /**
     * @return  the hostname of the node (ie "8.8.8.8")
     */
    public String getNodeHost();

    /**
     * @return  the port number of the node (ie 8080)
     */
    public int getNodePort();

    /**
     * @return  array of two strings representing the low and high range of the hashes that the given node is responsible for
     */
    public String[] getNodeHashRange();

    public void setMetaData(MetaData metaData);

    public void setNodeStatus(ECSNode.ECSNodeFlag flag);
    public byte[] toBytes();
}
