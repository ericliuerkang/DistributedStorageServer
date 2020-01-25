package app_kvServer;

import app_kvServer.KVServer;

public class KVServerThread implements Runnable {
    
	private KVServer server;
	
	public KVServerThread(KVServer server){
		this.server = server;
		new Thread(this).start();
	}
	
	public void run(){
		server.connection();
	}

}
