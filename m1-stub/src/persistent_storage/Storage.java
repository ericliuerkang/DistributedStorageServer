package persistent_storage;
import org.apache.log4j.Logger;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import shared.messages.KVMessageImplementation;
import java.io.RandomAccessFile;

public class Storage {
    private static Logger logger = Logger.getRootLogger();
    private Map<String, locationData> locationStorage;
    private int port;
    private String locationStorageFileName;
    private String DBName;

    public Storage(int port) {
        this.port = port;
    }

    public void initializeStorageData(){
        locationStorageFileName = "";
        DBName = "";
        locationStorage = new Collections.synchronizedMap(map);
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

    public void putLocationStorageData(String Key, String Value){

    }

    // Marshalling and Demarshalling of Data




}
