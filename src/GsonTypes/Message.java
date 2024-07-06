package GsonTypes;

import com.google.gson.Gson;

public class Message {

    public int tipGsona = 1;
    protected int tip; // {0, 1, 2} - public, private, system
    protected String sender;
    protected String reciever;
    protected String time;
    protected String body;

    public String createJson(int tip, String sender, String reciever, String time, String body) { // da v json, to klicemo vedno ko posiljamo clientu

        this.setTip(tip);
        this.setSender(sender);
        this.setReciever(reciever);
        this.setTime(time);
        this.setBody(body);

        Gson gson = new Gson();
        String jsonRequest = gson.toJson(this);

        return jsonRequest;
    }

    public int getTip() {

        return tip;
    }

    public void setTip(int tipA) {

        this.tip = tipA;
    }

    public String getSender() {

        return sender;
    }

    public void setSender(String senderA) {

        this.sender = senderA;
    }

    public String getReciever() {

        return reciever;
    }

    public void setReciever(String recieverA) {

        this.reciever = recieverA;
    }

    public String getTime() {

        return time;
    }

    public void setTime(String timeA) {

        this.time = timeA;
    }

    public String getBody() {

        return body;
    }

    public void setBody(String bodyA) {

        this.body = bodyA;
    }

}
