package client;

import logger.LogSetup;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import shared.communication.KVCommunication;
import shared.dataTypes.MetaData;
import shared.messages.KVMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

public class KVStore implements KVCommInterface {
	private static final String PROMPT = "Client> ";
	/**
	 * Initialize KVStore with address and port of KVServer
	 * @param address the address of the KVServer
	 * @param port the port of the KVServer
	 */
	private Logger logger = Logger.getRootLogger();
	private String address;
	private int port;

	private Socket clientSocket;
	private OutputStream output;
	private InputStream input;

	private KVCommunication communicationManager;
	private TreeMap<BigInteger, MetaData> metaData;


	public KVStore(String address, int port) {
		// TODO Auto-generated method stub
		this.port = port;
		this.address = address;
		logger.info("Connection Established");
	}
	/**
	 * @throws Exception
	 */
	@Override
	public void connect() throws Exception {
		clientSocket = new Socket(address, port);
		try{
			output = clientSocket.getOutputStream();
			input = clientSocket.getInputStream();
			communicationManager = new KVCommunication(clientSocket, null);
			logger.info("Connection Successful");
		}catch (IOException ioe) {
			logger.error("Connection could not be established!");
		}
	}

	@Override
	public void disconnect() {
		logger.info("try to close connection ...");
		try {
			tearDownConnection();
		} catch (Exception e) {
			logger.error("Unable to close connection! Error:" + e);
		}
	}

	private void tearDownConnection() throws IOException {
		logger.info("tearing down the connection ...");
		if (clientSocket != null) {
			input.close();
			output.close();
			clientSocket.close();
			clientSocket = null;
			logger.info("connection closed!");
		}
	}

	@Override
	public KVMessage put(String key, String value) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("hi");
		communicationManager.sendMessage(KVMessage.StatusType.PUT, key, value);
		return communicationManager.receiveMessage();
	}

	@Override
	public KVMessage get(String key) throws Exception {
		// TODO Auto-generated method stub
		communicationManager.sendMessage(KVMessage.StatusType.GET, key, null);
		return communicationManager.receiveMessage();
	}

	public void moveData(String value) throws Exception{
		communicationManager.sendMessage(KVMessage.StatusType.GET, "","", KVMessage.ECSType.ECS_Move_Data);
	}

	public void findResponsibleServer(String key){

	}

	public void hashCompare(BigInteger hashValue, TreeMap <BigInteger, MetaData> metaData){
		for (Map.Entry<BigInteger, MetaData> entry : metaData.entrySet()) {
			MetaData temp = entry.getValue();
			BigInteger upper = temp.getStartHash();
			BigInteger lower = temp.getEndHash();
			boolean descend = (upper.compareTo(lower) == 1);
			if (hashValue.compareTo(upper) == 0 ||
					hashValue.compareTo(lower) == 0 ||
					(hashValue.compareTo(upper) == 1 && hashValue.compareTo(lower) == -1 && !descend) ||
					(hashValue.compareTo(upper) == -1 && hashValue.compareTo(lower) == -1 && descend) ||
					(hashValue.compareTo(upper) == 1 && hashValue.compareTo(lower) == 1 && descend)) {
				// find the corresponding range
				this.port = temp.getPort();
				this.address = temp.getHost();
				break;
			}
		}
	}

	public boolean isRunning() throws IOException {
		return clientSocket != null && clientSocket.getInetAddress().isReachable(100);
	}

	public String setLevel(String levelString) {
		if(levelString.equals(Level.ALL.toString())) {
			logger.setLevel(Level.ALL);
			return Level.ALL.toString();
		} else if(levelString.equals(Level.DEBUG.toString())) {
			logger.setLevel(Level.DEBUG);
			return Level.DEBUG.toString();
		} else if(levelString.equals(Level.INFO.toString())) {
			logger.setLevel(Level.INFO);
			return Level.INFO.toString();
		} else if(levelString.equals(Level.WARN.toString())) {
			logger.setLevel(Level.WARN);
			return Level.WARN.toString();
		} else if(levelString.equals(Level.ERROR.toString())) {
			logger.setLevel(Level.ERROR);
			return Level.ERROR.toString();
		} else if(levelString.equals(Level.FATAL.toString())) {
			logger.setLevel(Level.FATAL);
			return Level.FATAL.toString();
		} else if(levelString.equals(Level.OFF.toString())) {
			logger.setLevel(Level.OFF);
			return Level.OFF.toString();
		} else {
			return LogSetup.UNKNOWN_LEVEL;
		}
	}


	public void printPossibleLogLevels() {
		System.out.println(PROMPT
				+ "Possible log levels are:");
		System.out.println(PROMPT
				+ "ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF");
	}
}