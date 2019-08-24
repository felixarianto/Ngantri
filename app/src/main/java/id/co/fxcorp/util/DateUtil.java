package id.co.fxcorp.util;

import android.text.format.DateUtils;

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

    public static String formatDateTime(long time) {
        return new SimpleDateFormat("dd/MM/yy HH:mm").format(time);
    }

    public static String formatDateReverse(long time) {
        return new SimpleDateFormat("yyMMdd").format(time);
    }

    public static boolean isSameDay(long a, long b) {
        Calendar acal = Calendar.getInstance();
        Calendar bcal = Calendar.getInstance();

        acal.setTimeInMillis(a);
        bcal.setTimeInMillis(b);

        return acal.get(Calendar.DAY_OF_YEAR) == bcal.get(Calendar.DAY_OF_YEAR) &&
               acal.get(Calendar.YEAR) == bcal.get(Calendar.YEAR)
               ;
    }



}
