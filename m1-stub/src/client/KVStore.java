package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.Socket;

import org.apache.log4j.Logger;
import shared.messages.KVMessage;

public class KVStore implements KVCommInterface {
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


	public KVStore(String address, int port) {
		// TODO Auto-generated method stub
		this.port = port;
		this.address = address;
		// setRunning(true);
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
		// setRunning(false);
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
		return null;
	}

	@Override
	public KVMessage get(String key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
