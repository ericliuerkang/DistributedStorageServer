package shared.dataTypes;

import ecs.ECSNode;
import ecs.IECSNode;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collection;

public class MetaData implements Serializable {
    private String name;
    private String host;
    private int port;
    private BigInteger startHash;
    private BigInteger endHash;
    private boolean writeLocked;

    public MetaData(String name, String host, int port,
                    BigInteger startHash, BigInteger endHash) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.startHash = startHash;
        this.endHash = endHash;
        writeLocked = false;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public BigInteger getStartHash() {
        return startHash;
    }

    public BigInteger getEndHash() {
        return endHash;
    }

    public boolean isWriteLocked() {
        return writeLocked;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setWriteLocked(boolean writeLocked) {
        this.writeLocked = writeLocked;
    }

    public void setNodeHashRange(BigInteger start, BigInteger end){
        this.startHash = start.add(new BigInteger("1"));
        this.endHash = end;
    }
}
