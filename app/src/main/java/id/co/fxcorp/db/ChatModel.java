package id.co.fxcorp.db;

public class ChatModel {

    public String id;
    public String group;
    public long   created_time;
    public String userid;
    public String name;
    public long   number;
    public String text;
    public String image;
    public long status;

    public final static long STATUS_SEND = 0;
    public final static long STATUS_DELIVERED = 1;

    public ChatModel clone() {
        ChatModel newObj = new ChatModel();
        newObj.id     = id;
        newObj.group  = group;
        newObj.created_time = created_time;
        newObj.userid = userid;
        newObj.name   = name;
        newObj.number = number;
        newObj.text   = text;
        newObj.image  = image;
        newObj.status = status;
        return newObj;
    }

}
