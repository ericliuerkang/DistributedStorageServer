package persistent_storage;

import java.io.Serializable;

public class locationData implements Serializable {
    private long length;
    private int startPoint;

    public locationData(int length, int startPoint) {
        this.length = length;
        this.startPoint = startPoint;
    }

    public long getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(int startPoint) {
        this.startPoint = startPoint;
    }

}
