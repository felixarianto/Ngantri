package id.co.fxcorp.ngantri;

import id.co.fxcorp.db.UserDB;

public class AppInfo {

    public static String getUserId(){
        return UserDB.MySELF.id;
    }

    public static String getUserName(){
        return UserDB.MySELF.name;
    }

    public static String getUserPhoto(){
        return UserDB.MySELF.photo;
    }
}
