package app_kvServer;

import com.google.gson.Gson;

public class ServerMetaData {
    private String cacheStratgy;
    private Integer cacheSize;
    private String host;
    private Integer receivingPort;

    public ServerMetaData(String cacheStratgy, Integer cacheSize, String host, Integer receivingPort) {
        this.cacheStratgy = cacheStratgy;
        this.cacheSize = cacheSize;
        this.host = host;
        this.receivingPort = receivingPort;
    }

    public String getCacheStratgy() {
        return cacheStratgy;
    }

    public void setCacheStratgy(String cacheStratgy) {
        this.cacheStratgy = cacheStratgy;
    }

    public Integer getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(Integer cacheSize) {
        this.cacheSize = cacheSize;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getReceivingPort() {
        return receivingPort;
    }

    public void setReceivingPort(Integer receivingPort) {
        this.receivingPort = receivingPort;
    }

    public String encode(){
        return new Gson().toJson(this);
    }

    public void decode(String msg){
        ServerMetaData smd = new Gson().fromJson(msg, this.getClass());
        this.host = smd.getHost();
        this.cacheSize = smd.getCacheSize();
        this.cacheStratgy = smd.getCacheStratgy();
        this.receivingPort = smd.getReceivingPort();
    }

}
