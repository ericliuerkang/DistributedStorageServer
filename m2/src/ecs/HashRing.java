package ecs;
import org.apache.log4j.Logger;
import shared.dataTypes.MD5;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class HashRing<T extends IECSNode> {
    public SortedMap<BigInteger, IECSNode> ring;
    private Logger logger = Logger.getRootLogger();
    private int size;

    public HashRing(){
        this.ring = new TreeMap<>();
        this.size = 0;
    }

    public HashRing(String packedMetaData){

    }

    public Set<Map.Entry<BigInteger, IECSNode>> getEntrySet() {return ring.entrySet();}

    public int getSize() {
        return size;
    }

    public void addNode(ECSNode ecsNode) throws NoSuchAlgorithmException {
        BigInteger hashValue = calculateHashValue(ecsNode.getNodeName());
        if (ring.isEmpty()){
            ecsNode.setNodeHashRange(hashValue, hashValue.subtract(new BigInteger("1")));
        }else {
            SortedMap<BigInteger, IECSNode> headMap = ring.headMap(hashValue);
            SortedMap<BigInteger, IECSNode> tailMap = ring.tailMap(hashValue);
            ECSNode biggerNode = (ECSNode) (tailMap.isEmpty() ? ring.get(ring.firstKey()) : ring.get(tailMap.firstKey()));
            ECSNode smallerNode = (ECSNode) (headMap.isEmpty() ? ring.get(ring.lastKey()) : ring.get(headMap.firstKey()));
            //Might be sketchy.
            smallerNode.setNodeHashRange(calculateHashValue(smallerNode.getNodeName()), hashValue);
            ecsNode.setNodeHashRange(hashValue, calculateHashValue(biggerNode.getNodeName()));
        }
        ring.put(calculateHashValue(ecsNode.getNodeHost()), ecsNode);
        size++;
    }

    public void removeNode(ECSNode ecsNode) throws NoSuchAlgorithmException {
        if (ring.containsKey(calculateHashValue(ecsNode.getNodeName()))){
            BigInteger hashValue = calculateHashValue(ecsNode.getNodeName());
            SortedMap<BigInteger, IECSNode> headMap = ring.headMap(hashValue);
            SortedMap<BigInteger, IECSNode> tailMap = ring.tailMap(hashValue);
            ECSNode biggerNode = (ECSNode) (tailMap.isEmpty() ? ring.get(ring.firstKey()) : ring.get(tailMap.firstKey()));
            ECSNode smallerNode = (ECSNode) (tailMap.isEmpty() ? ring.get(ring.lastKey()) : ring.get(headMap.firstKey()));
            //Removal
            smallerNode.setNodeHashRange(calculateHashValue(smallerNode.getNodeName()), calculateHashValue(biggerNode.getNodeName()));
            ring.remove(calculateHashValue(ecsNode.getNodeHost()));
        }
        size--;
    }

    public void removeNode(BigInteger hashedName) throws NoSuchAlgorithmException {
        if (ring.containsKey(hashedName)) {
            SortedMap<BigInteger, IECSNode> headMap = ring.headMap(hashedName);
            SortedMap<BigInteger, IECSNode> tailMap = ring.tailMap(hashedName);
            ECSNode biggerNode = (ECSNode) (tailMap.isEmpty() ? ring.get(ring.firstKey()) : ring.get(tailMap.firstKey()));
            ECSNode smallerNode = (ECSNode) (tailMap.isEmpty() ? ring.get(ring.lastKey()) : ring.get(headMap.firstKey()));
            //Removal
            smallerNode.setNodeHashRange(calculateHashValue(smallerNode.getNodeName()), calculateHashValue(biggerNode.getNodeName()));
            ring.remove(hashedName);
        }
        size--;
    }

    public ECSNode reassignNode(String key) throws NoSuchAlgorithmException {
        if (ring.isEmpty()) {
            return null;
        }
        BigInteger hashValue = calculateHashValue(key);
        SortedMap<BigInteger, IECSNode> map = ring.tailMap(hashValue);
        ECSNode node = (ECSNode) (map.isEmpty() ? ring.get(ring.firstKey()) : ring.get(map.firstKey()));
        return node;
    }

    public BigInteger calculateHashValue(String key) throws NoSuchAlgorithmException {
        return new BigInteger(MD5.getMD5EncryptedValue(key), 16);
    }

    public Map<String, IECSNode> getNodes(){
        Map<String, IECSNode> outMap = new HashMap<>();
        for (Map.Entry<BigInteger, IECSNode> entry : ring.entrySet()){
            outMap.put(entry.getValue().getNodeName(), entry.getValue());
        }
        return outMap;
    }

    public IECSNode getNode(BigInteger hashedName) {
        return ring.get(hashedName);
    }

    /*
    Use GSON to pack and unpack meta data.
     */
    public void packMetaData(){

    }

    public void unpackMetaData(){

    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        ECSNode a = new ECSNode("Server_1", "localhost", 1000);
        ECSNode b = new ECSNode("Server_2", "localhost", 1000);
        ECSNode c = new ECSNode("Server_3", "localhost", 1000);
        HashRing hr = new HashRing();
        hr.addNode(a);
        hr.addNode(b);
        hr.addNode(c);
        a.getNodeHashRange();
        b.getNodeHashRange();
        System.out.println();
        hr.removeNode(a);
        System.out.println();
    }

}
