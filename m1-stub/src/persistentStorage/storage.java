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
    //private Map<String, locationData> locationStorage;
    private int port;
    private String locationStorageFileName;
    private String DBName;
    // private RandomAccessFile serverFile;

    public storage(int port) {
        this.port = port;
        this.locationStorageFileName = port+"_look_up_table.txt";
        this.DBName = port + "_persistent_storage.txt";
        //this.locationStorage = Collections.synchronizedMap(new HashMap<String, locationData>());
        //Maybe use Treemap for better efficency.
    }

    /**
     *
     * @param DBName
     * @return
     */
    public RandomAccessFile loadDBFile(String DBName) {
        try {
            RandomAccessFile serverFile = new RandomAccessFile(DBName, "rw");
            return serverFile;
        } catch (FileNotFoundException FNE) {
            logger.info("Saving failed, FileNotFoundException: ", FNE);
        }
        return null;
    }

    /**
     * Serialize and save location storage data.
     * File cannot be opened and read by a text editor.
     * @param locationStorage
     * @throws IOException
     */
    public void saveLocationStorage(Map<String, locationData> locationStorage) throws IOException {
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(locationStorageFileName));
            os.writeObject(locationStorage);
            os.close();
            logger.info("serialized location data stored in " + locationStorage);
        } catch (FileNotFoundException FNE) {
            logger.error("Saving failed, FileNotFoundException: ", FNE);
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
        Map<String, locationData> locationStorage = loadLocationStorage(locationStorageFileName);
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

    public synchronized long writeCharsToFile(String message, String DBName, int location) throws IOException {
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
    /*
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
     */

    /**
     * Encode Message to Json using Gson.
     * @param s
     * @return
     */
    public String encodeMessage(storageData s){
        Gson gson = new Gson();
        String jsonString = gson.toJson(s);
        return jsonString;
    }

    /**
     *
     * @param bytesArray An array of bytes read from the
     * @return storageData, object that contains key, value, delete information and length of the entry
     */
    public storageData decodeBytes(byte[] bytesArray){
        Gson gson = new Gson();
        String s = new String(bytesArray);
        System.out.println(s);
        System.out.println(s.length());
        storageData sk = gson.fromJson(s, storageData.class);
        return sk;
    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     */
    public String getValue(String key) {
        try {
            Map<String, locationData> stringlocationDataHashMap = loadLocationStorage(locationStorageFileName);
            locationData loc = stringlocationDataHashMap.get(key);
            RandomAccessFile raf = loadDBFile(DBName);
            byte[] res = readCharsFromFile(loc.getStartPoint(), loc.getLength(), DBName);
            storageData sk = decodeBytes(res);
            if(sk.getDeleted() != 1){
                System.out.println(sk.getValue());
                return sk.getValue();
            }else{
                logger.error("Item Already deleted");
            }
        }catch(IOException ioe){
            logger.error(ioe);
            ioe.printStackTrace();
            System.out.println("help");
        }catch(NullPointerException NPE){
        	logger.error(NPE); 
        	NPE.printStackTrace(); 
        	
        }
        return null;
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
                writeCharsToFile(message, DBName, (int) raf.length());
                saveLocationStorage(stringlocationDataHashMap);

            }else{
                locationData loc = stringlocationDataHashMap.get(key);
                byte[] res = readCharsFromFile(loc.getStartPoint(), loc.getLength(), DBName);
                storageData s = decodeBytes(res);
                if (!s.getValue().equals(value)) {
                    s.setDeleted(1);
                    String message = encodeMessage(s);

                    writeCharsToFile(message, DBName, loc.getStartPoint());
                    storageData newS = new storageData(key, value);
                    String k = encodeMessage(newS);
                    System.out.println(k);
                    int tl = k.length();
                    newS.setTotalLength(tl + (int) (Math.log10(tl)));
                    message = encodeMessage(newS);

                    loc = new locationData(newS.getTotalLength(), (int) raf.length());
                    stringlocationDataHashMap.remove(key);

                    stringlocationDataHashMap.put(key, loc);
                    saveLocationStorage(stringlocationDataHashMap);
                    writeCharsToFile(message, DBName, (int) raf.length());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param key The key of the entry that is going to get deleted.
     * @throws IOException
     */
    public void deleteValue(String key) throws IOException {
            Map<String, locationData> stringlocationDataHashMap = loadLocationStorage(locationStorageFileName);
            RandomAccessFile raf = loadDBFile(DBName);
            if (stringlocationDataHashMap.containsKey(key)) {
                locationData loc = stringlocationDataHashMap.get(key);
                byte[] res = readCharsFromFile(loc.getStartPoint(), loc.getLength(), DBName);
                storageData s = decodeBytes(res);
                s.setDeleted(1);
                String message = encodeMessage(s);
                writeCharsToFile(message, DBName, loc.getStartPoint());
                stringlocationDataHashMap.remove(key);
                saveLocationStorage(stringlocationDataHashMap);
            } else {
                logger.info("The key: " + key +" does not exist.");
            }
    }

    public boolean inStorage(String key){
        Map<String, locationData> stringLocationDataHashMap = loadLocationStorage(locationStorageFileName);
        return stringLocationDataHashMap.containsKey(key);
    }


    /**
     * Clear look-up table
     */
    public void clearFile(){
        File f = new File(DBName);
        if (f.delete()){
            logger.info("Deleted Persistent Storage File " + f);
        }
        f = new File(locationStorageFileName);
        if (f.delete()){
            logger.info("Deleted Location Data Storage File " + f);
        }
    }

    public static void main(String[] args) {
        storage s = new storage(1);
        try {
            s.putValue("k", "vv");
            s.getValue("k");
            s.putValue("k", "vvvv");
            s.putValue("k", "vvvv");
            s.putValue("k", "vvvv");
            s.putValue("k", "vvvv");

            s.getValue("k");
            s.deleteValue("k");
            s.getValue("k");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // s.clearFile();
    }

}
