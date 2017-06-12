package com.bupt.adsystem.RemoteServer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.view.Settings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hadoop on 16-8-24.
 */
public class SettingsReceiver implements MessageTargetReceiver {

    private static final String TAG = "ImageDownloadReceiver";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private Context mContext;

    private JSONObject mJSONObject;

    @Override
    public String receiveMessage(MessageContext messageContext) {
        mContext = messageContext.getContext();
        mJSONObject = messageContext.getJSONObject();
        String indexPath = messageContext.getIndexPath();

        if (indexPath.equals(MessagePath.Settings.PREFIX + MessagePath.Settings.VOICE_ON_TIME)) {
            try {
                JSONObject jsonObject = mJSONObject.getJSONObject(MessagePath.KEY_PARAM);
                String startInterval = jsonObject.getString(MessagePath.KEY_INTERVAL_START);
                String endInterval = jsonObject.getString(MessagePath.KEY_INTERVAL_END);
                Settings.saveVoiceOnTimeSetting(mContext, startInterval, endInterval);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (indexPath.equals(MessagePath.Settings.PREFIX + MessagePath.Settings.VOLUME)) {
            try {
                int volume = mJSONObject.getInt(MessagePath.KEY_VOLUME);
                Settings.setSystemVolume(mContext, volume);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (indexPath.equals(MessagePath.Settings.PREFIX + MessagePath.Settings.DEVICE_INFO)) {

        }

        return null;
    }
}
