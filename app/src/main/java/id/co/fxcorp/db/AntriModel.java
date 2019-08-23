package id.co.fxcorp.db;

import id.co.fxcorp.util.DateUtil;

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
    public long   time;


    public boolean isComplete() {
        if (DateUtil.isSameDay(time, System.currentTimeMillis())) {
        }
        else if (time < System.currentTimeMillis()) {
            return true;//Expired
        }
        return status != null && status.matches("Selesai|Batal");
    }
}
