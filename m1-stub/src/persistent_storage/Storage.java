package persistent_storage;
import org.apache.log4j.Logger;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import shared.messages.KVMessageImplementation;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    private static Logger logger = Logger.getRootLogger();
    private ConcurrentHashMap<String, locationData>  locationStorage;
    private int port;
    private String locationStorageFileName;
    private String DBName;
    private RandomAccessFile serverFile;


    public Storage(int port) {
        this.port = port;
    }

    public void initializeStorageData() throws FileNotFoundException {
        locationStorageFileName = "";
        DBName = "";
        locationStorage = new ConcurrentHashMap<>();
        serverFile = new RandomAccessFile(DBName, "w+");
    }

    public void saveLocationStorage(Map<String, String> storage) throws  IOException{
        try{
            logger.info("Attempting to serialize data");
            Properties properties = new Properties();
            for (Map.Entry<String,String> entry : storage.entrySet()) {
                properties.put(entry.getKey(), entry.getValue());
            }
            properties.store(new FileOutputStream("data.properties"), null);
        }catch (FileNotFoundException FNE){
            logger.info("Saving failed, FileNotFoundException: ", FNE);
        }catch (IOException IOE){
            logger.info("Saving failed, IOException: ", IOE);
        }
    }

    public Map loadLocationStorage() throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        Properties properties = new Properties();
        properties.load(new FileInputStream("data.properties"));
        for (String key : properties.stringPropertyNames()) {
            map.put(key, properties.get(key).toString());
        }
        return map;
    }

    public void deleteLocationStorageData(String key){
        if (locationStorage.containsKey(key)){
            try{
                logger.info("Attempting to remove key: " + key);
                locationStorage.remove(key);
            }catch (Exception e){
                logger.error("DeleteKV failed ");
            }
        }else{
            logger.debug("Key is not found in locationStorage");
        }
    }

    public void putLocationStorageData(String Key, String Value) {
        if (locationStorage.containsKey(key)) {

        } else {
            int location = 0;
            locationData _locationData = new locationData(location, Value.length());
            logger.info("Created Key and Value");
        }
    }

    public synchronized long saveToRandomAccessFile(String message) throws IOException {
        long location = serverFile.length();
        serverFile.seek(location);
        serverFile.write(message.getBytes());
        logger.info("Write to File");
        return location;
    }

    public synchronized byte[] readCharsFromFile (String filePath, long location, int chars) throws IOException {
        serverFile.seek(location);
        byte[] bytes = new byte[chars];
        serverFile.read(bytes);
        serverFile.close();
        return bytes;
    }

    public



}
