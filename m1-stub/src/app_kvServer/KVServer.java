package app_kvServer;

import cache.KVCache;
import logger.LogSetup;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import persistentStorage.storage;
import shared.communication.KVCommunication;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;


public class KVServer implements IKVServer {

	private static Logger logger = Logger.getRootLogger();
	private int port;
	private int cacheSize;
	private ServerSocket serverSocket;
	private boolean running;
	private CacheStrategy cacheStrategy;
	private ArrayList<Thread> serverThreadList;
	private Thread serverThread;
	private KVCache cache;
	private storage storage;

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
		this.storage = new storage(port);
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
		if (inCache(key)){
			return cache.getV(key);
		}
		else{
			String value = storage.getValue(key);
			cache.putKV(key, value);
			return value;
		}
	}

	@Override
    public void putKV(String key, String value) throws Exception{
		cache.putKV(key, value);
		// TODO put storage stuff here
		storage.putValue(key, value);
	}

	public void deleteKV(String key) throws Exception{
		if (inCache(key)) {
			cache.deleteKV(key);
		}
		storage.deleteValue(key);
	}

	@Override
    public void clearCache(){
		// TODO Auto-generated method stub
		cache.clearCache();
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
