package app_kvClient;

import client.KVCommInterface;
import client.KVStore;
import logger.LogSetup;
import shared.messages.KVMessage;

import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class KVClient implements IKVClient {

    private static Logger logger = Logger.getRootLogger();
    private static final String PROMPT = "Client> ";
    private BufferedReader stdin;
    private boolean stop = false;

    private String serverAddress;
    private int serverPort;

    private KVStore kvstore = null;

    @Override
    public void newConnection(String hostname, int port) throws Exception{
        // TODO Auto-generated method stub
        new LogSetup("logs/client.log", Level.ALL);
        kvstore = new KVStore(hostname, port);
        kvstore.connect();
    }

    @Override
    public KVCommInterface getStore(){
        // TODO Auto-generated method stub
        return kvstore;
    }

    private void printError(String error){
        System.out.println(PROMPT + "Error! " +  error);
    }

    private void handleCommand(String cmdLine) throws IOException {
        String[] tokens = cmdLine.split("\\s+");

        if(tokens[0].equals("quit")) {
            stop = true;
            kvstore.disconnect();
            System.out.println(PROMPT + "Application exit!");
            logger.info(PROMPT + "Application exit!");

        } else if (tokens[0].equals("connect")){
            if(tokens.length == 3) {
                try{
                    serverAddress = tokens[1];
                    serverPort = Integer.parseInt(tokens[2]);
                    newConnection(serverAddress, serverPort);
                } catch(NumberFormatException nfe) {
                    printError("No valid address. Port must be a number!");
                    logger.info("Unable to parse argument <port>", nfe);
                } catch (UnknownHostException e) {
                    printError("Unknown Host!");
                    logger.info("Unknown Host!", e);
                } catch (IOException e) {
                    printError("Could not establish connection!");
                    logger.warn("Could not establish connection!", e);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                printError("Invalid number of parameters!");
            }

        } else if (tokens[0].equals("put")) {
            if(tokens.length >= 2) {
                if(kvstore != null && kvstore.isRunning()){
                    String key = tokens[1];
                    StringBuilder str = new StringBuilder();
                    for (int i = 2; i < tokens.length; i++)
                        str.append(tokens[i]);
                    String value = str.toString();
                    if (value != null){
                        try {
                            KVMessage ret = kvstore.put(key, value);
                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                } else {
                    printError("Not connected!");
                }
            } else {
                printError("Invalid number of parameters!!");
            }

        } else if(tokens[0].equals("disconnect")) {
            if (tokens.length == 1){
                kvstore.disconnect();
            } else {
                printError("Invalid number of parameters!");
            }

        } else if(tokens[0].equals("logLevel")) {
            if(tokens.length == 2) {
                String level = kvstore.setLevel(tokens[1]);
                if(level.equals(LogSetup.UNKNOWN_LEVEL)) {
                    printError("No valid log level!");
                    kvstore.printPossibleLogLevels();
                } else {
                    System.out.println(PROMPT +
                            "Log level changed to level " + level);
                }
            } else {
                printError("Invalid number of parameters!");
            }

        } else if(tokens[0].equals("help")) {
            printHelp();
        } else {
            printError("Unknown command");
            printHelp();
        }
    }

    public void run() {
        while (!stop) {
            stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(PROMPT);
            try {
                String cmdLine = stdin.readLine();
                this.handleCommand(cmdLine);
            } catch (IOException e) {
                stop = true;
                printError("Error! Application terminated. ");
            }

        }
    }

    private void printHelp(){
        logger.info("connect <ServerAddress> <PortNumber> ; put <key> <value>; get <key>; disconnect; help;");
    }

}
