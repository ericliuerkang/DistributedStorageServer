package persistentStorage;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class storage {
    private static Logger logger = Logger.getRootLogger();
    private Map<String, locationData> locationStorage;
    private int port;
    private String locationStorageFileName;
    private String DBName;
    // private RandomAccessFile serverFile;

    public storage(int port) {
        this.port = port;
        this.locationStorageFileName = port+"_look_up_table.txt";
        this.DBName = port + "_persistent_storage.txt";
        this.locationStorage = Collections.synchronizedMap(new HashMap<String, locationData>());
    }

    public RandomAccessFile loadDBFile(String DBName) {
        try {
            RandomAccessFile serverFile = new RandomAccessFile(DBName, "rw");
            return serverFile;
        } catch (FileNotFoundException FNE) {
            logger.info("Saving failed, FileNotFoundException: ", FNE);
        }
        return null;
    }

    public void saveLocationStorage(Map<String, locationData> storage) {
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(locationStorageFileName));
            os.writeObject(storage);
            os.close();
            logger.info("serialized location data stored in " + locationStorage);
        } catch (FileNotFoundException FNE) {
            logger.error("Saving failed, FileNotFoundException: ", FNE);
        } catch (IOException IOE) {
            logger.error("Saving failed, IOException: ", IOE);
        }
    }

    public Map loadLocationStorage(String locationStorageFileName) {
        try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(locationStorageFileName))) {
            HashMap<String, locationData> stringlocationDataHashMap = (HashMap<String, locationData>) is.readObject();
            is.close();
            return stringlocationDataHashMap;
        }catch (ClassNotFoundException CNFE) {
            CNFE.printStackTrace();
            logger.error("Loading failed, ClassNotFoundException", CNFE);
        }catch (IOException IOE) {
            IOE.printStackTrace();
            logger.error("Loading Failed, IOException", IOE);
        }
        return new HashMap<String, locationData>();
    }

    public void deleteLocationStorageData(String key) {
        //Only delete the entry from lookup table
        if (locationStorage.containsKey(key)) {
            try {
                logger.info("Attempting to remove key: " + key);
                locationStorage.remove(key);
                saveLocationStorage(locationStorage);
                logger.info("Key:"+ key +  " removal success");
            } catch (Exception e) {
                logger.error("DeleteKV failed ");
            }
        } else {
            logger.debug("Key is not found in locationStorage");
        }
    }

    public void putLocationStorageData(String key, String Value) {
        if (locationStorage.containsKey(key)) {
            return;
        } else {
            int location = 0;
            locationData _locationData = new locationData(location, Value.length());
            logger.info("Created Key and Value");
        }
    }

    public synchronized long saveToFile(String message, String DBName) throws IOException {
        RandomAccessFile serverFile = loadDBFile(DBName);
        long location = serverFile.length();
        serverFile.seek(location);
        serverFile.write(message.getBytes());
        logger.info("Write to File");
        return location;
    }

    public synchronized byte[] readCharsFromFile(long location, int charLength, String DBName) throws IOException {
        RandomAccessFile serverFile = loadDBFile(DBName);
        serverFile.seek(location);
        byte[] bytes = new byte[charLength];
        serverFile.read(bytes);
        serverFile.close();
        return bytes;
    }

    public void deleteFromFile(long location) {
        try{
            RandomAccessFile serverFile = loadDBFile(DBName);
            long position = 0;
            String line = null;
            while ((line = serverFile.readLine()) != null) {
                System.out.println("line::line.length()::position::getFilePointer()");
                System.out.println(line + "::" + line.length() + "::" + position + "::" + serverFile.getFilePointer());
                logger.info(line);
                int deleteFlag = line.charAt(0);
                if (deleteFlag == 1) {
                    //Create a byte[] to contain the remainder of the file.
                    byte[] remainingBytes = new byte[(int) (serverFile.length() - serverFile.getFilePointer())];
                    System.out.println("Remaining byte information::" + new String(remainingBytes));
                    serverFile.read(remainingBytes);
                    //Truncate the file to the position of where we deleted the information.
                    serverFile.getChannel().truncate(position);
                    System.out.println("Moving to beginning of line..." + position);
                    serverFile.seek(position);
                    serverFile.write(remainingBytes);
                    return;
                }
                position += serverFile.getFilePointer();
            }
        }catch (IOException e) {
            e.printStackTrace();
            logger.error("");
        }
    }

    public void encodeMessage(String key, String value){
        //Convert message from
        String recordSeparator = "/r/n";
        int recordSeparatorLength = recordSeparator.length();
        long valueLength = value.length();
        long keyLength = key.length();

    }

    public void decodeMessage(){

    }


}
