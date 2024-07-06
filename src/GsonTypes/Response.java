package GsonTypes;

import java.util.Collection;
import java.util.Map;

import com.google.gson.Gson;

public class Response {
    
    public int tipGsona = 3;

    // 0 - list of online users response
    // 1 - history response
    // 999 - showing offline messages to user (does not have Request counterpart)
    protected int tip;
    protected Collection<?> collection;
    protected Map<?, ?> map;
    protected String reciever; 
    protected String time;

    public <T, U, V> String createResponse(int tip, Collection<T> collection, Map<U, V> map, String reciever, String time) {

        this.setTip(tip);
        this.setCollection(collection);
        this.setMap(map);
        this.setReciever(reciever);
        this.setTime(time);

        Gson gson = new Gson();
        String jsonRequest = gson.toJson(this);

        return jsonRequest;
    }

    public int getTip() {

        return this.tip;
    }

    public void setTip(int tip) {

        this.tip = tip;
    }

    public Collection<?> getCollection() {

        return this.collection;
    }

    public <T> void setCollection(Collection<T> collection) {

        this.collection = collection;
    }

    public Map<?, ?> getMap() {

        return this.map;
    }

    public <U, V> void setMap(Map<U, V> map) {

        this.map = map;
    }

    public void setReciever(String reciever) {

        this.reciever = reciever;
    }

    public String getReciever() {

        return this.reciever;
    }

    public void setTime(String time) {

        this.time = time;
    }

    public String getTime() {

        return this.time;
    }
}
