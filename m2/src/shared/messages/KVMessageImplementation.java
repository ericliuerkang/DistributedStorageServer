package shared.messages;

import java.io.Serializable;

public class KVMessageImplementation implements KVMessage, Serializable {

    private StatusType status;
    private String key;
    private String value;

    public KVMessageImplementation(StatusType status, String key, String value)
    {
        this.status = status;
        this.key = key;
        this.value = value;
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
