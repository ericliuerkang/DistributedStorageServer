package ecs;
import java.io.Serializable;
import java.math.BigInteger;

public class ECSNode implements  IECSNode, Serializable{
    public enum ECSNodeFlag{
        STOP,
        START,
        STATE_CHANGE,
        KV_TRANSFER,
        SHUT_DOWN,
        UPDATE,
        TRANSFER_FINISH
    }
    private String name;
    private String host;
    private int port;
    private BigInteger hashStart;
    private BigInteger hashEnd;

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


}
