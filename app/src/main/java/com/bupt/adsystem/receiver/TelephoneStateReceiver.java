package com.bupt.adsystem.receiver;

import com.android.internal.telephony.*;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bupt.adsystem.Utils.AdSystemConfig;

import java.lang.reflect.Method;

/**
 * Created by hadoop on 16-8-6.
 */
public class TelephoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = "TelephoneStateReceiver";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    TelephonyManager mTelMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        mTelMgr = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        if (DEBUG) Log.d(TAG, intent.getAction());
        endCall();

//        abortBroadcast();
    }

    /**
     * 挂断电话
     */
    private void endCall()
    {
        Class<TelephonyManager> c = TelephonyManager.class;
        try
        {
            Method getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null);
            getITelephonyMethod.setAccessible(true);
            ITelephony iTelephony = null;
            Log.e(TAG, "End call.");
            iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelMgr, (Object[]) null);
//            iTelephony.endCall();
            iTelephony.answerRingingCall();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Fail to answer ring call.", e);
        }
    }


}
