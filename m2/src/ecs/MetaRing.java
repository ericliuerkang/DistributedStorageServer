package ecs;

import shared.dataTypes.MD5;
import shared.dataTypes.MetaData;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class MetaRing<T extends MetaData> {
    public SortedMap<BigInteger, MetaData> ring;

    public MetaRing(){
        this.ring = new TreeMap<>();
    }

    public MetaRing(String packedMetaData){

    }

    public Set<Map.Entry<BigInteger, MetaData>> getEntrySet() {return ring.entrySet();}

    public void addNode(MetaData md) throws NoSuchAlgorithmException {
        BigInteger hashValue = calculateHashValue(md.getName());
        if (ring.isEmpty()){
            md.setNodeHashRange(hashValue, hashValue.subtract(new BigInteger("1")));
        }else {
            SortedMap<BigInteger, MetaData> headMap = ring.headMap(hashValue);
            SortedMap<BigInteger, MetaData> tailMap = ring.tailMap(hashValue);
            MetaData biggerNode = (tailMap.isEmpty() ? ring.get(ring.firstKey()) : ring.get(tailMap.firstKey()));
            MetaData smallerNode = (tailMap.isEmpty() ? ring.get(ring.lastKey()) : ring.get(headMap.firstKey()));
            //Might be sketchy.
            smallerNode.setNodeHashRange(smallerNode.getStartHash(), hashValue);
            md.setNodeHashRange(hashValue, biggerNode.getStartHash());
        }
        ring.put(calculateHashValue(md.getName()), md);
    }

    public BigInteger getNextHash(ECSNode node){
        BigInteger hashValue = null;
        try {
            hashValue = calculateHashValue(node.getNodeName());
            SortedMap<BigInteger, MetaData> tailMap = ring.tailMap(hashValue);
            MetaData biggerNode = (tailMap.isEmpty() ? ring.get(ring.firstKey()) : ring.get(tailMap.firstKey()));
            return calculateHashValue(biggerNode.getName());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BigInteger getPrevHash(ECSNode node){
        BigInteger hashValue = null;
        try {
            hashValue = calculateHashValue(node.getNodeName());
            SortedMap<BigInteger, MetaData> headMap = ring.headMap(hashValue);
            SortedMap<BigInteger, MetaData> tailMap = ring.tailMap(hashValue);
            MetaData smallerNode = (tailMap.isEmpty() ? ring.get(ring.lastKey()) : ring.get(headMap.firstKey()));
            return calculateHashValue(smallerNode.getName());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BigInteger getPrevHash(BigInteger hashValue){
        try {
            SortedMap<BigInteger, MetaData> headMap = ring.headMap(hashValue);
            SortedMap<BigInteger, MetaData> tailMap = ring.tailMap(hashValue);
            MetaData smallerNode = (tailMap.isEmpty() ? ring.get(ring.lastKey()) : ring.get(headMap.firstKey()));
            return calculateHashValue(smallerNode.getName());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeNode(MetaData md) throws NoSuchAlgorithmException {
        if (ring.containsKey(calculateHashValue(md.getName()))){
            BigInteger hashValue = calculateHashValue(md.getName());
            SortedMap<BigInteger, MetaData> headMap = ring.headMap(hashValue);
            SortedMap<BigInteger, MetaData> tailMap = ring.tailMap(hashValue);
            MetaData biggerNode = (tailMap.isEmpty() ? ring.get(ring.firstKey()) : ring.get(tailMap.firstKey()));
            MetaData smallerNode = (tailMap.isEmpty() ? ring.get(ring.lastKey()) : ring.get(headMap.firstKey()));
            //Removal
            smallerNode.setNodeHashRange(smallerNode.getStartHash(), biggerNode.getStartHash());
            ring.remove(calculateHashValue(md.getName()));
        }
    }

//    public ECSNode reassignNode(String key) throws NoSuchAlgorithmException {
//        if (ring.isEmpty()) {
//            return null;
//        }
//        BigInteger hashValue = calculateHashValue(key);
//        SortedMap<BigInteger, MetaData> map = ring.tailMap(hashValue);
//        ECSNode node = (ECSNode) (map.isEmpty() ? ring.get(ring.firstKey()) : ring.get(map.firstKey()));
//        return node;
//    }

    public static BigInteger calculateHashValue(String key) throws NoSuchAlgorithmException {
        return new BigInteger(MD5.getMD5EncryptedValue(key));
    }

    public MetaData getMetaData(String key){
        try {
            return ring.get(calculateHashValue(key));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public MetaData getMetaData(BigInteger hashedKey){
        return ring.get(hashedKey);
    }

    /*
    Use GSON to pack and unpack meta data.
     */
    public void packMetaData(){

    }

    public void unpackMetaData(){

    }

    public static void main(String[] args) {

    }

}
