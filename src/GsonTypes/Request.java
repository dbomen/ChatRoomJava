package GsonTypes;

import com.google.gson.Gson;

public class Request {
    
    public int tipGsona = 2;
    
    // 0 - list of online users request
    // 1 - history request
    // 
    // 996 - sendFriendRequest | Send a friend request to someone, they Accept or Decline
    // 997 - removeFriendRequest | Declined the friend Request
    // 998 - removeFriend (confirmation is GsonTypes.Message) | Removing Friend
    // 999 - addFriend (confirmation is GsonTypes.Message) | Accepted the friend Request
    protected int tip;
    protected String sender;
    protected String otherInfo; 
    protected String time;

    public String createRequest(int tip, String sender, String otherInfo, String time) {

        this.setTip(tip);
        this.setSender(sender);
        this.setOtherInfo(otherInfo);
        this.setTime(time);
        
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(this);

        return jsonRequest;
    }

    public void setTip(int tip) {

        this.tip = tip;
    }

    public int getTip() {

        return this.tip;
    }

    public void setSender(String sender) {

        this.sender = sender;
    }

    public String getSender() {

        return this.sender;
    }

    public void setOtherInfo(String otherInfo) {

        this.otherInfo = otherInfo;
    }

    public String getOtherInfo() {

        return this.otherInfo;
    }

    public void setTime(String time) {

        this.time = time;
    }

    public String getTime() {

        return this.time;
    }
}
