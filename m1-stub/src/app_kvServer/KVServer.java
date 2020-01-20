package app_kvServer;

import shared.communication.*;

import java.net.*;
import java.io.IOException;

import logger.LogSetup;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class KVServer implements IKVServer {
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
	private static Logger logger = Logger.getRootLogger();
	private int port;
	private ServerSocket serverSocket;
	private boolean running;
	private int cacheSize;
	private CacheStrategy cacheStrategy;
	private ArrayList<Thread> serverThreadList;
	private Thread serverThread;

	public KVServer(int port, int cacheSize, String strategy) {
		// TODO Auto-generated method stub
		this.port = port;
		this.cacheSize = cacheSize;
		this.cacheStrategy = stringToStrategy(strategy);
		serverThreadList = new ArrayList<Thread>();
		serverThread = null;
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
		try {
			InetAddress host = InetAddress.getLocalHost();
			return host.getHostName();
		} catch (UnknownHostException uhe) {
			logger.error("Error! Failed to get host name.", uhe);
			return null;
		}
	}

	@Override
    public CacheStrategy getCacheStrategy(){
		// TODO Auto-generated method stub
		return this.cacheStrategy;
	}

	@Override
    public int getCacheSize(){
		// TODO Auto-generated method stub
		return this.cacheSize;
	}

	@Override
    public boolean inStorage(String key){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
    public boolean inCache(String key){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
    public String getKV(String key) throws Exception{
		// TODO Auto-generated method stub
		return "";
	}

	@Override
    public void putKV(String key, String value) throws Exception{
		// TODO Auto-generated method stub
	}

	@Override
    public void clearCache(){
		// TODO Auto-generated method stub
	}

	@Override
    public void clearStorage(){
		// TODO Auto-generated method stub
	}

	@Override
    public void run(){
		// TODO Auto-generated method stub
		running = initializeServer();
		if (serverSocket != null) {
			while (isRunning()) {
				try {
					Socket client = serverSocket.accept();

					KVCommunication communicationManager = new KVCommunication(client, this);
					Thread serverThread = new Thread (communicationManager);
					serverThread.start();
					serverThreadList.add(serverThread);

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
				new KVServer(port, cacheSize, args[2]).run();
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
