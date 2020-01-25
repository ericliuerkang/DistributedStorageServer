package persistentStorage;

public class storageData {
    private int deleted;
    private String key;
    private String value;
    private int totalLength;


    public storageData(String key, String value) {
        this.key = key;
        this.value = value;
        this.deleted = 0;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public String getKey() {
        return key;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
