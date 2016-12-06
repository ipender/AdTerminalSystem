package com.bupt.adsystem.view;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.RemoteServer.AlarmUtil;

import org.xutils.BuildConfig;
import org.xutils.x;

/**
 * Created by wyouflf on 15/10/28.
 */
public class MyApplication extends Application {

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private static MyApplication sApplication;
    private static Context mContext;

    @Override
    public void onCreate() {
        sApplication = this;
        super.onCreate();

        mContext = getApplicationContext();

        SharedPreferences appSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        appSettings.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

        Settings.Voice.OnTime = appSettings.getString(Settings.Voice.KEY_ON_TIME, "07:00:00");
        Settings.Voice.OffTime = appSettings.getString(Settings.Voice.KEY_OFF_TIME, "21:00:00");
        Settings.Voice.Volume = appSettings.getInt(Settings.Voice.KEY_Volume, 10);

        Settings.Device.Id = appSettings.getString(Settings.Device.KEY_ID, "12345");
        Settings.Device.RunMode = appSettings.getString(Settings.Device.KEY_RunMode, "install");
        Settings.Device.NetType = appSettings.getString(Settings.Device.KEY_NetType, "4G");
        Settings.Device.InfraredType = appSettings.getString(Settings.Device.KEY_InfraredType, "NormalOpen");
        Settings.Device.NumOfFloors = appSettings.getInt(Settings.Device.KEY_NumOfFloors, 10);
        Settings.Device.FloorDefinition = appSettings.getString(Settings.Device.KEY_FloorDefinition,
                "-2,-1,G,2,3,5,6,7，8，9");
        Settings.Device.SpeedLimit = appSettings.getFloat(Settings.Device.KEY_SpeedLimit, 2.0f);
        Settings.Device.DoorOpenTime = appSettings.getFloat(Settings.Device.KEY_DoorOpenTime, 30);

        Settings.SystemInit(mContext);
        AlarmUtil.initAlarmWhenStartUp(mContext);

        x.Ext.init(this);
        x.Ext.setDebug(true); // 是否输出debug日志, 开启debug会影响性能.
    }

    public static MyApplication instance() {
        return sApplication;
    }

    private OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Settings.Voice.KEY_ON_TIME) || key.equals(Settings.Voice.KEY_OFF_TIME)) {
                Settings.Voice.OnTime = sharedPreferences.getString(Settings.Voice.KEY_ON_TIME, "07:00:00");
                Settings.Voice.OffTime = sharedPreferences.getString(Settings.Voice.KEY_OFF_TIME, "21:00:00");
            } else if (key.equals(Settings.Voice.KEY_Volume)) {
                Settings.Voice.Volume = sharedPreferences.getInt(Settings.Voice.KEY_Volume, 50);
            } else if (key.equals(Settings.Device.Id)) {

            } else if (key.equals(Settings.Device.KEY_RunMode)) {

            } else if (key.equals(Settings.Device.KEY_NetType)) {

            } else if (key.equals(Settings.Device.KEY_InfraredType)) {

            } else if (key.equals(Settings.Device.KEY_NumOfFloors)) {

            } else if (key.equals(Settings.Device.KEY_FloorDefinition)) {

            } else if (key.equals(Settings.Device.KEY_SpeedLimit)) {

            } else if (key.equals(Settings.Device.KEY_DoorOpenTime)) {

            }
        }
    };

}
