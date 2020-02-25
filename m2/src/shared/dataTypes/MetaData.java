package shared.dataTypes;

import ecs.IECSNode;

import java.io.Serializable;
import java.util.Collection;

public class MetaData implements Serializable {
    private String name;
    private String host;
    private int port;
    private String startHash;
    private String endHash;
    private boolean writeLocked;
    private Collection<IECSNode> iecsNodes;

    public MetaData(String name, String host, int port,
                    String startHash, String endHash) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.startHash = startHash;
        this.endHash = endHash;
        writeLocked = false;
        this.iecsNodes = null;
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

    public String getStartHash() {
        return startHash;
    }

    public String getEndHash() {
        return endHash;
    }

    public boolean isWriteLocked() {
        return writeLocked;
    }

    public void addNode(IECSNode newNode) { iecsNodes.add(newNode);}
}
