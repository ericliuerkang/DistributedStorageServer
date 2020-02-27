package ecs;

import shared.dataTypes.MD5;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class HashRing<T extends IECSNode> {
    public SortedMap<BigInteger, IECSNode> ring;

    public HashRing(){
        this.ring = new TreeMap<>();
    }

    public HashRing(String packedMetaData){

    }

    public void addNode(ECSNode ecsNode) throws NoSuchAlgorithmException {
        BigInteger hashValue = calculateHashValue(ecsNode.getNodeHost());
        if (ring.isEmpty()){
            ecsNode.setNodeHashRange(hashValue, hashValue.subtract(new BigInteger("1")));
        }else {
            SortedMap<BigInteger, IECSNode> headMap = ring.headMap(hashValue);
            SortedMap<BigInteger, IECSNode> tailMap = ring.tailMap(hashValue);
            ECSNode biggerNode = (ECSNode) (tailMap.isEmpty() ? ring.get(ring.firstKey()) : ring.get(tailMap.firstKey()));
            ECSNode smallerNode = (ECSNode) (tailMap.isEmpty() ? ring.get(ring.lastKey()) : ring.get(headMap.firstKey()));
            //Might be sketchy.
            smallerNode.setNodeHashRange(calculateHashValue(smallerNode.getNodeName()), hashValue);
            ecsNode.setNodeHashRange(hashValue, calculateHashValue(biggerNode.getNodeName()));
        }
        ring.put(calculateHashValue(ecsNode.getNodeHost()), ecsNode);
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
    }

    public ECSNode reassignNode(String key) throws NoSuchAlgorithmException {
        if (ring.isEmpty()){
            return null;
        }
        BigInteger hashValue = calculateHashValue(key);
        SortedMap<BigInteger, IECSNode> map = ring.tailMap(hashValue);
        ECSNode node = (ECSNode) (map.isEmpty() ? ring.get(ring.firstKey()) : ring.get(map.firstKey()));
        return node;
    }

    public BigInteger calculateHashValue(String key) throws NoSuchAlgorithmException {
        return new BigInteger(MD5.getMD5EncryptedValue(key));
    }

    public packMetaData(){

    }

    public unpackMetaData(){

    }

    public

    public static void main(String[] args) {

    }

}
