package com.bupt.adsystem.Utils;

import android.content.Context;
import com.bupt.adsystem.R;

/**
 * Created by hadoop on 17-6-25.
 */
public class Property {

    private Property() {
    }

    public static void init(Context context) {
        RTMP_PULL_URL = context.getString(R.string.rtmp_pull_url);
        RTMP_PUSH_URL = context.getString(R.string.rtmp_push_url);

    }

    public static String RTMP_PUSH_URL;
    public static String RTMP_PULL_URL;



}
