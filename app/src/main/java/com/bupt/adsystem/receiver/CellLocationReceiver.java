package com.bupt.adsystem.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bupt.adsystem.Utils.AdSystemConfig;

/**
 * Created by hadoop on 16-8-6.
 */
public class CellLocationReceiver extends BroadcastReceiver{
    private static final String TAG = "CellLocationReceiver";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String location = bundle.getString("CellLocation");
        if (DEBUG) Log.d(TAG, "CellLocation is:" + location);
    }
}
