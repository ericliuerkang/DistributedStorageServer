package ecs;

import shared.dataTypes.MD5;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class HashRing<T extends IECSNode> {
    public SortedMap<BigInteger, IECSNode> ring = new TreeMap<>();

    public HashRing(SortedMap<BigInteger, IECSNode> ring) {
        this.ring = ring;
    }

    public void addNode(ECSNode ecsNode) throws NoSuchAlgorithmException {
        ring.put(calculateHashValue(ecsNode.getNodeHost()), ecsNode);
    }

    public void removeNode(ECSNode ecsNode) throws NoSuchAlgorithmException {
        ring.remove(calculateHashValue(ecsNode.getNodeHost()));
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



    public static void main(String[] args) {

    }

}
