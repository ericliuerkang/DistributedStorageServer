package persistentStorage;

import com.google.gson.Gson;
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
        //Maybe use Treemap for better efficency.
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
            Map<String, locationData> stringlocationDataHashMap = Collections.synchronizedMap( (Map<String, locationData>)is.readObject());
            is.close();
            return stringlocationDataHashMap;
        }catch (ClassNotFoundException CNFE) {
            CNFE.printStackTrace();
            logger.error("Loading failed, ClassNotFoundException", CNFE);
        } catch(FileNotFoundException FNE){

        } catch (IOException IOE) {
            IOE.printStackTrace();
            logger.error("Loading Failed, IOException", IOE);
        }
        return Collections.synchronizedMap(new HashMap<String, locationData>());
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

    public synchronized long writeToFile(String message, String DBName, int location) throws IOException {
        RandomAccessFile serverFile = loadDBFile(DBName);
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

    public String encodeMessage(storageData s){
        //Convert message from
        /*
        String recordSeparator = "/r/n";
        int recordSeparatorLength = recordSeparator.length();
        long valueLength = value.length();
        long keyLength = key.length();

        byte[] keyBytes = key.getBytes();
        byte[] valueBytes = value.getBytes();

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        byte[] keyLengthArray = buffer.putLong(keyLength).array();
        byte[] valueBytes =
         */
        Gson gson = new Gson();
        String jsonString = gson.toJson(s);
        return jsonString;
    }

    public storageData decodeBytes(byte[] bytesArray){
        /*
        String recordSeparator = "/r/n";
        int recordSeparatorLength = recordSeparator.length();
         */
        Gson gson = new Gson();
        String s = new String(bytesArray);
        System.out.println(s);
        System.out.println(s.length());
        storageData sk = gson.fromJson(s, storageData.class);
        return sk;
    }

    public String getValue(String key) throws IOException {
        Map<String, locationData> stringlocationDataHashMap = loadLocationStorage(locationStorageFileName);
        locationData loc = stringlocationDataHashMap.get(key);
        RandomAccessFile raf = loadDBFile(DBName);
        byte[] res = readCharsFromFile(loc.getStartPoint(), loc.getLength(), DBName);
        storageData sk = decodeBytes(res);
        return sk.getValue();
    }

    public void putValue(String key, String value){
        try {
            Map<String, locationData> stringlocationDataHashMap = loadLocationStorage(locationStorageFileName);
            RandomAccessFile raf = loadDBFile(DBName);
            if (!stringlocationDataHashMap.containsKey(key)) {
                byte[] valueByte = value.getBytes();
                storageData s = new storageData(key, value);
                String k = encodeMessage(s);
                int tl = k.length();
                s.setTotalLength(tl + (int) (Math.log10(tl)));
                locationData loc = new locationData(s.getTotalLength(), (int) raf.length());

                stringlocationDataHashMap.put(key, loc);

                String message = encodeMessage(s);
                writeToFile(message, DBName, (int) raf.length());
                saveLocationStorage(stringlocationDataHashMap);

            }else{
                locationData loc = stringlocationDataHashMap.get(key);
                byte[] res = readCharsFromFile(loc.getStartPoint(), loc.getLength(), DBName);
                storageData s = decodeBytes(res);
                if (!s.getValue().equals(value)) {
                    s.setDeleted(1);
                    String message = encodeMessage(s);

                    writeToFile(message, DBName, loc.getStartPoint());
                    storageData newS = new storageData(key, value);
                    String k = encodeMessage(newS);
                    System.out.println(k);
                    int tl = k.length();
                    newS.setTotalLength(tl + (int) (Math.log10(tl)));
                    message = encodeMessage(newS);

                    loc = new locationData(newS.getTotalLength(), (int) raf.length());
                    stringlocationDataHashMap.put(key, loc);
                    saveLocationStorage(stringlocationDataHashMap);
                    writeToFile(message, DBName, (int) raf.length());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearFile(){

    }

    public static void main(String[] args) {
        storage s = new storage(1);
        s.putValue("k", "vv");
        s.putValue("k", "vvvv");

    }

}