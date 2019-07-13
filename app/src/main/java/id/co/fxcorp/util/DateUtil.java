package id.co.fxcorp.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {

    public static String formatTime(long time) {
        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time);
        if (       cal.get(Calendar.DATE) == cal.get(Calendar.DATE)
                && cal.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                && cal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {

            return new SimpleDateFormat("HH:mm").format(time);
        }
        else {
            return new SimpleDateFormat("dd/MM/yy HH:mm").format(time);
        }
    }

    public static String formatDate(long time) {
        return new SimpleDateFormat("dd/MM/yy").format(time);
    }

    public static String formatDateReverse(long time) {
        return new SimpleDateFormat("yyyy/MM/dd").format(time);
    }

}
