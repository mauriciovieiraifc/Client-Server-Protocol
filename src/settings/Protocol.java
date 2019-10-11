package settings;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Communication explanation:
 * 
 * Client sends a REQ and waits for ACK and RES from the server
 * If client receives ACK and RES, client sends ACK to server and the connection is closed
 * If Client.AYA_TIME expires, client sends AYA and waits for IAA from the server
 * If Server.delay >= Server.ERROR_TIME, server sends TA and client try to send 
 * the message again
 */
public class Protocol implements Serializable {

    private final Map<String, Object> params;

    public Protocol() {
        this.params = new HashMap<>();
    }
    
    public Object getParam(String key) {
        return params.get(key);
    }
    
    public void setParam(String key, Object value) {
        params.put(key, value);
    }
    
    public void clear() {
        this.params.clear();
    }
    
    @Override
    public String toString() {
        String p = "{";        
        p = params.entrySet().stream().map((entry) 
                -> entry.getKey() + " " + entry.getValue() + ", ")
                    .reduce(p, String::concat);
        
        String substring = p.substring(0, p.length() - 2);
        substring += "}";
        
        return substring;
    }
}
