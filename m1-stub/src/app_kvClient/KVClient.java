package app_kvClient;

import client.KVCommInterface;
import client.KVStore;

public class KVClient implements IKVClient {
    private KVStore kvstore = null;

    @Override
    public void newConnection(String hostname, int port) throws Exception{
        // TODO Auto-generated method stub
        kvstore = new KVStore(hostname, port);
        kvstore.connect();
    }

    @Override
    public KVCommInterface getStore(){
        // TODO Auto-generated method stub
        return kvstore;
    }



}
