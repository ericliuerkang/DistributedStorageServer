package ecs;

import java.util.Collection;
import java.util.TreeMap;
import java.security.MessageDigest;

public class HashRing<T extends IECSNode> {
    private final SortedMap<Long, ECSNode<T>> ring = new TreeMap<Long, ECSNode<T>>();

    public HashRing(Collection<T> pNodes, int vNodeCount) {
        this(pNodes,vNodeCount, new MD5Hash());
    }

}
