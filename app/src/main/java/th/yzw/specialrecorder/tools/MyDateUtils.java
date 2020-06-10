package th.yzw.specialrecorder.tools;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public final class MyDateUtils {
    public static long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;
    private static Calendar c = new GregorianCalendar(Locale.CHINA);

    //获取指定日期0点的时间戳
    public static long getZero(long time) {
        return (time - (time + TimeZone.getDefault().getRawOffset()) % ONE_DAY_MILLIS);
    }

    public static long getDateDiff() {
        c.set(2020, 5, 1, 0, 0, 0);
        long time = c.getTimeInMillis();
        return (System.currentTimeMillis() - time) / 1000;
    }

    public static long getDateInMillis(long dateDiff) {
        c.set(2020, 5, 1, 0, 0, 0);
        long time = c.getTimeInMillis();
        return time + dateDiff * 1000;
    }

    public static long[] getMonthStartAndEndLong(long date) {
        long[] times = new long[2];
        c.setTimeInMillis(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();
        long end = start + c.getActualMaximum(Calendar.DAY_OF_MONTH) * ONE_DAY_MILLIS - 1;
        times[0] = start;
        times[1] = end;
        return times;
    }

    public static String[] getMonthStartAndEnd(long date) {
        String[] times = new String[2];
        long[] _times = getMonthStartAndEndLong(date);
        times[0] = String.valueOf(_times[0]);
        times[1] = String.valueOf(_times[1]);
        return times;
    }

    public static long[] getDayStartAndEndLong(long date) {
        long[] times = new long[2];
        c.setTimeInMillis(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();
        long end = start + ONE_DAY_MILLIS - 1;
        times[0] = start;
        times[1] = end;
        return times;
    }

    public static String[] getDayStartAndEnd(long date) {
        String[] times = new String[2];
        long[] _times = getDayStartAndEndLong(date);
        times[0] = String.valueOf(_times[0]);
        times[1] = String.valueOf(_times[1]);
        return times;
    }

    public static long[] getYearStartAndEndLong(long date) {
        long[] times = new long[2];
        c.setTimeInMillis(date);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();
        long end = start + c.getActualMaximum(Calendar.DAY_OF_YEAR) * ONE_DAY_MILLIS - 1;
        times[0] = start;
        times[1] = end;
        return times;
    }

    public static String[] getYearStartAndEnd(long date) {
        String[] times = new String[2];
        long[] _times = getYearStartAndEndLong(date);
        times[0] = String.valueOf(_times[0]);
        times[1] = String.valueOf(_times[1]);
        return times;
    }

    public static boolean isSameDay(long day1, long day2) {
        Calendar c = new GregorianCalendar(Locale.CHINA);
        c.setTimeInMillis(day1);
        int year1 = c.get(Calendar.YEAR);
        int month1 = c.get(Calendar.MONTH);
        int dayOfMonth1 = c.get(Calendar.DAY_OF_MONTH);
        c.setTimeInMillis(day2);
        int year2 = c.get(Calendar.YEAR);
        int month2 = c.get(Calendar.MONTH);
        int dayOfMonth2 = c.get(Calendar.DAY_OF_MONTH);
        return (year1 == year2) && (month1 == month2) && (dayOfMonth1 == dayOfMonth2);
    }
}
