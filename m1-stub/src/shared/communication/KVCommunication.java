package shared.communication;

import app_kvServer.KVServer;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import shared.messages.KVMessage;
import shared.messages.KVMessageImplementation;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class KVCommunication implements Runnable {

    private static Logger logger = Logger.getRootLogger();

    private static final int BUFFER_SIZE = 1024;
    private Socket clientSocket;
    private KVServer server;
    private boolean open;
    private InputStream inputStream;
    private OutputStream outputStream;

    public KVCommunication(Socket clientSocket, KVServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.open = true;
        try {
            this.inputStream = clientSocket.getInputStream();
            this.outputStream = clientSocket.getOutputStream();
        }
        catch (IOException ioe) {
            logger.error("Error! Failed to establish connection.", ioe);

        }
    }

    public void run() {
        try {
            if (server != null) {
                while (open) {
                    try {
                        KVMessage messageReceived = receiveMessage();
                        KVMessage messageToSend = null;
                        try {
                            messageToSend = handleMessage(messageReceived);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        assert messageToSend != null;
                        sendMessage(messageToSend.getStatus(), messageToSend.getKey(), messageToSend.getValue());
                    } catch (IOException ioe) {
                        logger.error("Error! Server or client lost." + ioe);
                        open = false;
                    }
                }
            }

        }
        finally {
            try {
                if (clientSocket != null) {
                    inputStream.close();
                    outputStream.close();
                    clientSocket.close();
                }
            } catch (IOException ioe) {
                logger.error("Error! Failed to close connection.", ioe);
            }
        }
    }

    public KVMessage receiveMessage() throws IOException {

        byte[] messageBuffer = new byte[BUFFER_SIZE];

        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int messageSize = dataInputStream.readInt();

        byte[] byteMessage = new byte[messageSize];

        int numBufferNeeded = messageSize / BUFFER_SIZE;
        int numBytesLeft = messageSize % BUFFER_SIZE;

        for (int i = 0; i < numBufferNeeded; i++) {
            try {
                int numBytesRead = inputStream.read(messageBuffer, 0, BUFFER_SIZE);
                assert(numBytesRead == BUFFER_SIZE);
            } catch (IOException ioe) {
                logger.error("Error! Failed to read message." + ioe);
            }
            System.arraycopy(messageBuffer, 0, byteMessage, i * BUFFER_SIZE, BUFFER_SIZE);

            messageBuffer = new byte[BUFFER_SIZE];
        }

        try {
            int numBytesRead = inputStream.read(messageBuffer, 0, numBytesLeft);
            assert(numBytesRead == numBytesLeft);
        } catch (IOException ioe) {
            logger.error("Error! Failed to read message." + ioe);
        }
        System.arraycopy(messageBuffer, 0, byteMessage, numBufferNeeded * BUFFER_SIZE, numBytesLeft);

        KVMessage messageReceived = null;
        try {
            JSONObject jsonMessage = new JSONObject(new String(byteMessage));
            String key = null;
            String value = null;
            String status = null;
            if (jsonMessage.has("key"))
                key = jsonMessage.getString("key");
            if (jsonMessage.has("value"))
                value = jsonMessage.getString("value");
            if (jsonMessage.has("status"))
                status = jsonMessage.getString("status");

            messageReceived = new KVMessageImplementation(stringToStatus(status), key, value);
            logger.info("Received Message: Status is " + messageReceived.getStatus() + "; Key is " + messageReceived.getKey() + "; Value is " + messageReceived.getValue());
        } catch (JSONException je) {
            logger.error("Error! Failed to create JSON object." + je);
        }

        return messageReceived;
    }

    private KVMessage handleMessage(KVMessage messageReceived) {
        KVMessage messageToSend = null;

        try{
            if (messageReceived != null){
                String key = messageReceived.getKey();
                String value = messageReceived.getValue();
                KVMessage.StatusType status = messageReceived.getStatus();
                KVMessage.StatusType resultStatus;
                switch (status){
                    case PUT:
                        if (key == null || key.contains(" ") || key.equals("") || key.length() < 1 || key.length() > 20) {
                            messageToSend = new KVMessageImplementation(KVMessage.StatusType.PUT_ERROR, key, value);
                        }
                        else {
                            if (value != null && !value.equals("null") && !value.equals("") && value.length() <= 120) {
                                if (server.inCache(key) || server.inStorage(key))
                                    resultStatus = KVMessage.StatusType.PUT_UPDATE;
                                else
                                    resultStatus = KVMessage.StatusType.PUT_SUCCESS;
                                server.putKV(key, value);
                                messageToSend = new KVMessageImplementation(resultStatus, key, value);
                            }
                            else{
                                //delete
                                if (server.inCache(key) || server.inStorage(key)) {
                                    server.deleteKV(key);
                                    resultStatus = KVMessage.StatusType.DELETE_SUCCESS;
                                } else {
                                    resultStatus = KVMessage.StatusType.DELETE_ERROR;
                                }
                                messageToSend = new KVMessageImplementation(resultStatus, key, value);
                            }
                        }
                        break;
                    case GET:
                        if (key == null || key.contains(" ") || key.equals("") || key.length() < 1 || key.length() > 20 || key.equals(null) || key.equals("null")) {
                            messageToSend = new KVMessageImplementation(KVMessage.StatusType.GET_ERROR, key, null);
                        }
                        else {
                        	System.out.print("hi3"); 
                            String valueToSend = server.getKV(key);
                            if (valueToSend != null){
                                resultStatus = KVMessage.StatusType.GET_SUCCESS;
                            }
                            else{
                                resultStatus = KVMessage.StatusType.GET_ERROR;
                            }
                            messageToSend = new KVMessageImplementation(resultStatus, key, valueToSend);
                        }
                        break;
                    default:
                        resultStatus = KVMessage.StatusType.FAILED;
                        messageToSend = new KVMessageImplementation(resultStatus, null, null);
                        break;
                }
            }
        } catch (Exception e) {
        	e.printStackTrace(); 
            messageToSend = new KVMessageImplementation(KVMessage.StatusType.FAILED, null, null);
        }

        return messageToSend;
    }

    public void sendMessage(KVMessage.StatusType status, String key, String value) throws IOException {
        KVMessage MessageToSend = new KVMessageImplementation(status, key, value);
        logger.info("Sent Message: Status is " + MessageToSend.getStatus() + "; Key is " + MessageToSend.getKey() + "; Value is " + MessageToSend.getValue());

        JSONObject jsonMessage = new JSONObject();
        try{
            jsonMessage.put("key", MessageToSend.getKey());
            jsonMessage.put("value", MessageToSend.getValue());
            jsonMessage.put("status", MessageToSend.getStatus());
        } catch (JSONException je){
            logger.error("Error! Failed to create JSON object." + je);
        }
        byte[] byteMessage = jsonMessage.toString().getBytes(StandardCharsets.UTF_8);

        // send bytes array length
        int messageSize = byteMessage.length;
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(messageSize);
        dataOutputStream.flush();

        //Send to server
        outputStream.write(byteMessage, 0, messageSize);
        outputStream.flush();
    }

    public void closeCommunication(){
        try {
            if (clientSocket != null) {
                inputStream.close();
                inputStream.close();
                clientSocket.close();
            }
        }catch (IOException ioe) {
            logger.error("Error! Failed to close connection.", ioe);
        }
    }

    private KVMessage.StatusType stringToStatus(String str){
        switch (str){
            case "PUT":
                return KVMessage.StatusType.PUT;
            case "PUT_SUCCESS":
                return KVMessage.StatusType.PUT_SUCCESS;
            case "PUT_UPDATE":
                return KVMessage.StatusType.PUT_UPDATE;
            case "PUT_ERROR":
                return KVMessage.StatusType.PUT_ERROR;
            case "GET":
                return KVMessage.StatusType.GET;
            case "GET_ERROR":
                return KVMessage.StatusType.GET_ERROR;
            case "GET_SUCCESS":
                return KVMessage.StatusType.GET_SUCCESS;
            case "DELETE_SUCCESS":
                return KVMessage.StatusType.DELETE_SUCCESS;
            case "DELETE_ERROR":
                return KVMessage.StatusType.DELETE_ERROR;
            default:
                return null;
        }
    }
}