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
        SOAP_WEB_SERVICE_URL = context.getString(R.string.soap_web_service_url);
        SOAP_NAME_SPACE = context.getString(R.string.soap_name_space);
        HTTP_GET_UPLOAD_INFO_URL_BASE = context.getString(R.string.http_get_upload_info_url);
        FILE_SERVER_URL = context.getString(R.string.file_server_url_base);
        AD_STRATEGY_FILE_URL_BASE = context.getString(R.string.ad_strategy_file_url_base);
        ADMIN_PHONE = context.getString(R.string.administrator_phone);
    }

    public static String RTMP_PUSH_URL;
    public static String RTMP_PULL_URL;

    public static String SOAP_WEB_SERVICE_URL;
    public static String SOAP_NAME_SPACE;
    public static String HTTP_GET_UPLOAD_INFO_URL_BASE;
    public static String FILE_SERVER_URL;
    public static String AD_STRATEGY_FILE_URL_BASE;
    public static String ADMIN_PHONE;



}
