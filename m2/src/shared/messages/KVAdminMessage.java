package shared.messages;

import com.google.gson.Gson;
import java.util.Arrays;

public class KVAdminMessage{
    public enum Command{
        INIT,
        SHUT_DOWN,
        START,
        STOP,
        TRANSFER
    }
    private Command command;


}
