package com.bupt.adsystem.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bupt.adsystem.view.MainActivity;

/**
 * Created by hadoop on 16-11-2.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = "com.bupt.adsystem.MainActivity";
        Intent toMainIntent = new Intent(context, MainActivity.class);
        toMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d("START", "Start when boot completed!");
        context.startActivity(toMainIntent);
    }
}
