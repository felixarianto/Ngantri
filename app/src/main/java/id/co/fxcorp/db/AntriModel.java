package id.co.fxcorp.db;

public class AntriModel {

    public String id;
    public String cust_id;
    public String cust_name;
    public String cust_photo;
    public long   number;
    public long   call_count;
    public String call_msg;
    public String place_id;
    public String place_name;
    public String place_photo;
    public String status;
    public long   created_time;


    public boolean isComplete() {
        return status != null && status.equals("Selesai");
    }
}
