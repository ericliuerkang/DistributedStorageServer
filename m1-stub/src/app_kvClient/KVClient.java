package app_kvClient;

import client.KVCommInterface;
import java.net.Socket;

import org.apache.log4j.Logger;



public class KVClient implements IKVClient {
    @Override
    private Socket clientSocket;
    private Logger logger = Logger.getRootLogger();

    public void newConnection(String hostname, int port) throws Exception{
        // TODO Auto-generated method stub
        clientSocket = new Socket(address, port);
        logger.info("Connection Established")
    }

    @Override
    public KVCommInterface getStore(){
        // TODO Auto-generated method stub
        return null;
    }
}
