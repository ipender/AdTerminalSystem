package com.bupt.adsystem.Utils;


import android.text.format.Time;
import android.util.Log;

import java.util.GregorianCalendar;

/**
 * Created by hadoop on 16-8-23.
 */
public class Utils {

    /**
     * 获取当前系统时间，时分秒，格式为： XX:XX:XX
     */
    @Deprecated
    public static String getCurrentTime() {
        Time time = new Time();
        time.setToNow();
        return String.format("%02d:%02d:%02d", time.hour, time.minute, time.second);
    }

    public static boolean isTimeInInterval(String time, String timeStart, String timeEnd) {

        boolean flag;
        if (time.compareTo(timeStart) >= 0 && time.compareTo(timeEnd) <= 0) flag = true;
        else flag = false;
        Log.d("TimeUtils", time + "  " + timeStart + "  " + timeEnd + "  " + flag);

        return flag;
    }
}
