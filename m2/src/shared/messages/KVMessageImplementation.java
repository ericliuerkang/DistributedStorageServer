package shared.messages;

import shared.dataTypes.MetaData;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.TreeMap;

public class KVMessageImplementation implements KVMessage, Serializable {

    private StatusType status;
    private String key;
    private String value;
    public ECSType ecsType;
    private TreeMap<BigInteger, MetaData> metaData;

    public KVMessageImplementation(StatusType status, String key, String value)
    {
        this.status = status;
        this.key = key;
        this.value = value;
        this.metaData = null;
        this.ecsType = null;
    }

    public KVMessageImplementation(StatusType status, String key, String value, ECSType ecsType)
    {
        this.status = status;
        this.key = key;
        this.value = value;
        this.ecsType = ecsType;
        this.metaData = null;


    }

    @Override
    public String getKey(){
        return this.key;
    }

    @Override
    public String getValue(){
        return this.value;
    }

    @Override
    public StatusType getStatus(){
        return this.status;
    }

}
