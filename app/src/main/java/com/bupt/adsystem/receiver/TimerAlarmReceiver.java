package com.bupt.adsystem.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bupt.adsystem.Utils.AdImageCtrl;
import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.Utils.AdVideoCtrl;
import com.bupt.adsystem.RemoteServer.AlarmUtil;

/**
 * Created by hadoop on 16-8-21.
 */
public class TimerAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "TimerAlarmReceiver";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(AlarmUtil.VideoIntervalStart)){
            if (DEBUG) Log.d(TAG, "VideoIntervalStart");
            AdVideoCtrl.getInstance().updateWhenAlarmUp(true);

        } else if (action.equals(AlarmUtil.VideoIntervalEnd)) {
            if (DEBUG) Log.d(TAG, "VideoIntervalEnd");
            AdVideoCtrl.getInstance().updateWhenAlarmUp(false);

        } else if (action.equals(AlarmUtil.ImageIntervalStart)) {
            if (DEBUG) Log.d(TAG, "ImageIntervalStart");
            AdImageCtrl.getInstanceIfExists().updateWhenAlarmUp(true);

        } else if (action.equals(AlarmUtil.ImageIntervalEnd)) {
            if (DEBUG) Log.d(TAG, "ImageIntervalEnd");
            AdImageCtrl.getInstanceIfExists().updateWhenAlarmUp(false);

        } else if (action.equals(AlarmUtil.SystemVoiceOn)) {
            if (DEBUG) Log.d(TAG, "SystemVoiceOn");

        } else if (action.equals(AlarmUtil.SystemVoiceOff)) {
            if (DEBUG) Log.d(TAG, "SystemVoiceOff");

        } else if (action.equals(AlarmUtil.SystemRestart)) {
            if (DEBUG) Log.d(TAG, "SystemRestart");

        }
        if (DEBUG) Log.d(TAG, action);
    }
}
