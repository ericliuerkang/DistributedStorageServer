package app_kvServer;

import cache.KVCache;
import client.KVStore;
import com.google.gson.Gson;
import ecs.ECSNode;
import ecs.HashRing;
import logger.LogSetup;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import persistentStorage.LocationData;
import persistentStorage.Storage;
import shared.communication.KVCommunication;
import shared.dataTypes.MetaData;
import zooKeeper.ZooKeeperString;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class KVServer implements IKVServer {
	private static Logger logger = Logger.getRootLogger();
	private String zkHostName;
	private int zkPort;
	private String name;
	private ZooKeeper zk;
	private String zkPath;


	private int port;
	private int cacheSize;
	private ServerSocket serverSocket;
	private boolean running;
	private CacheStrategy cacheStrategy;
	private ArrayList<Thread> serverThreadList;
	private Thread serverThread;
	private KVCache cache;
	private Storage storage;
	private HashRing hr;

	public enum ServerStateType {
		IDLE,                    /*server is idle*/
		STARTED,           /*server is started*/
		SHUT_DOWN,    /*server is shut down*/
		STOPPED           /*default server status; server is stopped*/
	}
	/**
	 * Start KV Server at given port
	 * @param port given port for storage server to operate
	 * @param cacheSize specifies how many key-value pairs the server is allowed
	 *           to keep in-memory
	 * @param strategy specifies the cache replacement strategy in case the cache
	 *           is full and there is a GET- or PUT-request on a key that is
	 *           currently not contained in the cache. Options are "FIFO", "LRU",
	 *           and "LFU".
	 */
	public KVServer(int port, int cacheSize, String strategy) {
		// TODO Auto-generated method stub
		this.port = port;
		this.cacheSize = cacheSize;
		this.cache = new KVCache(strategy, cacheSize);
		this.cacheStrategy = stringToStrategy(strategy);
		serverThreadList = new ArrayList<Thread>();
		serverThread = null;
		this.storage = new Storage(port);
		this.hr = new HashRing();
	}

	public KVServer(String name, String zkHostName, int zkPort){
		this.zkHostName = zkHostName;
		this.name = name;
		this.zkPort = zkPort;
		this.zkPath = ZooKeeperString.ZK_SERVER_ROOT + "/" + name;
		String connectString = this.zkHostName + ":" + Integer.toString(this.zkPort);

		//Connecting to Zookeeper
		try {
			final CountDownLatch sig = new CountDownLatch(1);
			zk = new ZooKeeper(connectString, ZooKeeperString.ZK_TIMEOUT, event -> {
				if (event.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
					// connection fully established can proceed
					sig.countDown();
				}
			});
			try {
				sig.await();
			} catch (InterruptedException e) {
				// Should never happen
				e.printStackTrace();
			}

		} catch (IOException e) {
			logger.debug(this.name + "Unable to connect to zookeeper");
			e.printStackTrace();
		}
		//Creating the Group ZNode
		try{
			if (zk.exists(zkPath, false) == null){
				logger.error("the zNode does not exist");
			}else{
				byte[] cacheData = zk.getData(zkPath, false, null);
				String cacheString = new String(cacheData);
				MetaData m = new Gson().fromJson(cacheString, MetaData.class);
				this.cacheSize = m.getCacheSize();
				this.cacheStrategy = CacheStrategy.valueOf(m.getCacheStrategy());
			}
		}catch (KeeperException | InterruptedException e){
			logger.error(this.name + " Unable to retrieve cache info from " + zkPath);
			this.cacheStrategy = CacheStrategy.FIFO;
			this.cacheSize = 100;
			e.printStackTrace();
		}

		//Update HashRing
		try{

		}catch{

		}


	}

	@Override
	public int getPort(){
		// TODO Auto-generated method stub
		if (serverSocket != null)
			return this.serverSocket.getLocalPort();
		else {
			System.out.println("Error! Failed to get port number.");
			return -1;
		}
	}

	@Override
    public String getHostname(){
		// TODO Auto-generated method stub
		if (serverSocket != null)
			return serverSocket.getInetAddress().getHostName();
		else {
			System.out.println("Error! Failed to get host name.");
			return null;
		}
	}

	@Override
    public CacheStrategy getCacheStrategy(){
		return this.cacheStrategy;
	}

	@Override
    public int getCacheSize(){
		return this.cacheSize;
	}

	@Override
    public boolean inStorage(String key){
		// TODO Auto-generated method stub
		return storage.inStorage(key);
	}

	@Override
    public boolean inCache(String key){
		return cache.inCache(key);
	}

	@Override
    public String getKV(String key) throws Exception{
		// TODO deal with exception and add storage from Jerry
		if (cache!=null && cache.getMaxCacheSize() > 0){
			if (inCache(key)){
				return cache.getV(key);
			}
			else{
				String value = storage.getValue(key);
				cache.putKV(key, value);
				return value;
			}
		}
		else{
			logger.info("No Cache during get");
			return storage.getValue(key);
		}
	}

	@Override
    public void putKV(String key, String value) throws Exception{
		try{
			// System.out.println("Key, Value: " + key + " | "+ value);
			if (cache!=null && cache.getMaxCacheSize() != 0){
				cache.putKV(key, value);
			}
			else{
				logger.info("No Cache during put");
			}
			//System.out.println("Checking Storage for put");
			storage.putValue(key, value);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void deleteKV(String key) throws Exception{
		if (cache!=null && cache.getMaxCacheSize() > 0){
			if (inCache(key)) {
				cache.deleteKV(key);
			}
		}
		else{ logger.info("No Cache during delete");}
		//System.out.println("Checking Storage for deletion");
		storage.deleteValue(key);
	}

	@Override
    public void clearCache(){
		if (cache!=null) {
			if (cache.getCurrentCacheSize() > 0) {
				cache.clearCache();
			}
		}
		else{
			logger.info("No Cache to clear");
		}
	}

	@Override
    public void clearStorage(){
		// TODO Auto-generated method stub
		cache.clearCache();
		storage.clearFile();
	}

	@Override
	public void run(){
		// TODO Auto-generated method stub
		new KVServerThread(this);
	}
	
    public void connection(){
		running = initializeServer();
		//System.out.println("help");
		if (serverSocket != null) {
			while (isRunning()) {
				try {
					Socket client = serverSocket.accept();

					KVCommunication communicationManager = new KVCommunication(client, this);
					Thread communication = new Thread (communicationManager);
					communication.start();
					serverThreadList.add(communication);

					logger.info("Connected to "
							+ client.getInetAddress().getHostName()
							+ " on port " + client.getPort());
				} catch (IOException e) {
					logger.error("Error! Failed to establish connection.");
				}
			}
		}
		logger.info("Server terminated.");
	}

	@Override
    public void kill(){
		// TODO Auto-generated method stub
		running = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.error("Error! " +
					"Unable to close socket on port: " + port, e);
		}
	}

	@Override
    public void close(){
		// TODO Auto-generated method stub
		running = false;
		try {
			for (int i = 0; i < serverThreadList.size(); i++){
				serverThreadList.get(i).interrupt();
			}
			if (serverThread != null)
				serverThread.interrupt();
			serverSocket.close();
		} catch (IOException e) {
			logger.error("Error! " +
					"Unable to close socket on port: " + port, e);
		}
	}

	private CacheStrategy stringToStrategy(String strategy) {
		switch (strategy) {
			case "LRU":
				return CacheStrategy.LRU;
			case "FIFO":
				return CacheStrategy.FIFO;
			case "LFU":
				return CacheStrategy.LFU;
			default:
				logger.error("Invalid Cache Strategy!");
				return CacheStrategy.None;
		}
	}

	public void addServer(String addedServerAddress, int addedServerPort, int existingServerPort) throws Exception{
		String existingServerName = existingServerPort +"_look_up_table.txt";
		Map<String, LocationData> existingServerlocationDataHashMap = storage.loadLocationStorage(existingServerName);
		KVStore tempClient = new KVStore(addedServerAddress, addedServerPort);
		for(Map.Entry<String, LocationData> entry : existingServerlocationDataHashMap.entrySet()) {
			String key = entry.getKey();
			String value = getKV(key);
			BigInteger hashValue = hr.calculateHashValue(key);
			if (hr.reassignNode(key).getNodePort() == addedServerPort && hr.reassignNode(key).getNodeName() == addedServerAddress);{
				tempClient.put(key, value);
				storage.deleteValue(key);
			}
			tempClient.put(key, value);
		}
	}

	public void removeServer(int removedServerPort, int responsibleServerPort, String responsibleAddress) throws  Exception{
		String removedServerLocationData = removedServerPort+"_look_up_table.txt";
		Map<String, LocationData> removedServerlocationDataHashMap = storage.loadLocationStorage(removedServerLocationData);
		String responsibleServerLocationData = responsibleServerPort+"_look_up_table.txt";
		Map<String, LocationData> responsibleServerlocationDataHashMap = storage.loadLocationStorage(responsibleServerLocationData);
		KVStore tempClient = new KVStore(responsibleAddress, responsibleServerPort);
		for(Map.Entry<String, LocationData> entry : removedServerlocationDataHashMap.entrySet()) {
			String key = entry.getKey();
			String value = getKV(key);
			tempClient.put(key, value);
		}
		clearStorage();
		clearCache();
	}

	private boolean isRunning() {
		return this.running;
	}

	private boolean initializeServer() {
		logger.info("Initialize server ...");
		try {
			serverSocket = new ServerSocket(port);
			logger.info("Server listening on port: "
					+ serverSocket.getLocalPort());
			return true;

		} catch (IOException e) {
			logger.error("Error! Cannot open server socket:");
			if (e instanceof BindException) {
				logger.error("Port " + port + " is already bound!");
			}
			return false;
		}
	}

	public static void main(String[] args) {
		try {
			new LogSetup("logs/server.log", Level.ALL);
			if(args.length != 3) {
				System.out.println("Error! Invalid number of arguments!");
				System.out.println("Usage: Server <port> <cacheSize> <strategy>.");
			} else {
				int port = Integer.parseInt(args[0]);
				int cacheSize = Integer.parseInt(args[1]);
				String cacheStrategy = args[2];
				new KVServer(port, cacheSize, cacheStrategy).run();
			}
		} catch (IOException e) {
			System.out.println("Error! Unable to initialize logger!");
			e.printStackTrace();
			System.exit(1);
		} catch (NumberFormatException nfe) {
			System.out.println("Error! Invalid argument <port>! Not a number!");
			System.out.println("Usage: Server <port> <cacheSize> <strategy>.");
			System.exit(1);
		}
	}
}
