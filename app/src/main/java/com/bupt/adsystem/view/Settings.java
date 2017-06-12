package com.bupt.adsystem.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bupt.adsystem.RemoteServer.MessagePath;
import com.bupt.adsystem.Utils.AdSystemConfig;

import java.util.Set;

/**
 * Created by hadoop on 16-8-20.
 * <p/>
 * SettingsReceiver Class中全是静态成员变量及静态方法
 * 所以此类是非线程安全的，只能在主线程中进行读写
 * 不可在其他线程中读写
 */
public class Settings {
    private static final String TAG = "Settings";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    static String mDeviceId = "12345";
    static String mRunningMode = "install";           // 终端运行模式
    static String mNetworkStandard;                 // 网络制式

    static int mTotalFloorNum = 10;
    static float mSpeedLimitation = 2.0f;           // unit: m/s
    static float mDoorOpenTime = 60f;               // unit： s

    static String mInfraredType = "OpenNormally";   // enum: OpenNormally CloseNormally
    static String mFloorDefinition = "-2,-1,G,2,3,5,6,7，8，9";       // 楼层显示时，完全按照此字符串来显示

    static String mPhoneNumber = "";                //

    static String mElevatorId = "00000000000000000000";             // 20 bit Decimal digit
    static String mElevatorModelType;
    static String mElevatorName = "";
    static String mElevatorType = "";
    static String mAreaType;
    static String mAreaLocation;

    static String mOperationsUnit;                          //运营单位
    static String mUserUnit;                                //使用单位
    static String mMaintenanceUnit;                         //维保单位
    static String mTenements;                               //物业单位
    static String mManufacturers;                           //厂家单位
    static String mSupervisorUnit;                          //监管单位
    static int mMaintenancePeriod;                          // unit: day
    static int MPeriodicInspection;                         // unit: year

    public static class Device {
        public static String KEY_ID = "DeviceId";
        public static String KEY_RunMode = "RunMode";
        public static String KEY_NetType = "NetType";
        public static String KEY_InfraredType = "InfraredType";
        public static String KEY_NumOfFloors = "NumOfFloors";
        public static String KEY_FloorDefinition = "FloorDefinition";
        public static String KEY_SpeedLimit = "SpeedLimit";
        public static String KEY_DoorOpenTime = "DoorOpenTime";

        public static String Id;
        public static String RunMode;          // 终端运行模式,有：install normal
        public static String NetType;           // 网络制式
        public static String InfraredType;      // 红外类型：NormalOpen 或 NormalClose
        public static int NumOfFloors;
        public static String FloorDefinition;   // 楼层显示时，完全按照此字符串来显示 "-2,-1,G,2,3,5,6,7，8，9"
        public static float SpeedLimit;         // unit: m/s  default:2.0m/s
        public static float DoorOpenTime;       // unit： s   default: 30s
    }

    public static class Elevator {
        public static String KEY_ID = "ElevatorId";
        public static String KEY_Name = "ElevatorName";
        public static String KEY_DoorType = "ElevatorType";
        public static String KEY_ModelType = "ElevatorModelType";
        public static String KEY_AreaType = "AreaType";
    }

    public static class Voice {
        public static String KEY_ON_TIME = "ON_TIME";
        public static String KEY_OFF_TIME = "OFF_TIME";
        public static String KEY_Volume = MessagePath.KEY_VOLUME;

        public static String OnTime;
        public static String OffTime;
        public static int Volume;
    }

    public static class System {
        public static String KEY_UPDATE_SERVER = "UPDATE_SERVER";
        public static String KEY_RESTART_TIME = "RESTART_TIME";

        public static Set<String> ServerList;
        public static String RestartTime = "24:00:00";
    }

    public static void saveVoiceOnTimeSetting(Context context, String startInterval, String endInterval) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Voice.KEY_ON_TIME, startInterval);
        editor.putString(Voice.KEY_OFF_TIME, endInterval);
        editor.commit();
    }

    public static void setSystemVolume(Context context, int volume) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int settingVolume = maxVolume * volume / 100;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, settingVolume, 0);

        if (DEBUG) Log.d(TAG, "MaxVolume:" + maxVolume + "SettingVoulume:" + settingVolume);
    }

    public static void SystemInit(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0);     // 通话音量
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Voice.Volume, 0);          // 多媒体音量
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 0, 0);
        setSystemVolume(context, Voice.Volume);
    }
}
