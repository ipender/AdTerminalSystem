package com.bupt.adsystem.RemoteServer;

import android.content.Context;

import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by hadoop on 16-8-13.
 */
public class MessageContext {

    private Context mContext;
    private JSONObject mJSONObject;
    private String mIndexPath;
    private String mResponseString = null;

    public String getScheduleId() {
        return mScheduleId;
    }

    public void setScheduleId(String scheduleId) {
        mScheduleId = scheduleId;
    }

    private String mScheduleId;

    public MessageContext(Context context, String responseString) {
        this.mContext = context;
        this.mResponseString = responseString;
    }

    public MessageContext(Context context, JSONObject jsonObject) {
        this.mContext = context;
        this.mJSONObject = jsonObject;
    }

    public Context getContext() {
        return mContext;
    }

    public JSONObject getJSONObject() {
        return mJSONObject;
    }

    public String getIndexPath() {
        return mIndexPath;
    }

    public void setIndexPath(String indexPath) {
        mIndexPath = indexPath;
    }

    public String getResponseString() {
        return mResponseString;
    }

    public String getInputStream() {
        return mResponseString;
    }
}
