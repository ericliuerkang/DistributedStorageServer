package ecs;
import shared.dataTypes.MetaData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.TreeMap;

public class ECSNode implements  IECSNode, Serializable{
    public ECSNodeFlag nodeStatus;

    public enum ECSNodeFlag{
        STOP,
        START,
        STATE_CHANGE,
        KV_TRANSFER,
        SHUT_DOWN,
        UPDATE,
        IDLE
    }
    private String name;
    private String host;
    private int port;
    private BigInteger hashStart;
    private BigInteger hashEnd;
    private MetaData metaData;

    public ECSNode(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.hashEnd = null;
        this.hashStart = null;
    }

    @Override
    public String getNodeName() {
        return name;
    }

    @Override
    public String getNodeHost() {
        return host;
    }

    @Override
    public int getNodePort() {
        return port;
    }

    public MetaData getMetaData() { return metaData; }

    @Override
    public String[] getNodeHashRange() {
        String[] hashRange = new String[2];
        hashRange[0] = hashStart.toString();
        hashRange[1] = hashEnd.toString();
        return hashRange;
    }

    public void setNodeHashRange(BigInteger start, BigInteger end){
        this.hashStart = start.add(new BigInteger("1"));
        this.hashEnd = end;
    }

    @Override
    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public void setNodeStatus(ECSNodeFlag flag){this.nodeStatus = flag;}

    @Override
    public byte[] toBytes(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] bytes = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            bytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return bytes;
    }

}
