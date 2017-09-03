package com.bupt.adsystem.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bupt.adsystem.Camera.CameraApp;
import com.bupt.adsystem.Camera.UVCCameraEnumerator;
import com.bupt.adsystem.R;
import com.bupt.adsystem.RemoteServer.NetUtil;
import com.bupt.adsystem.RemoteServer.ServerRequest;
import com.bupt.adsystem.Utils.AdImageCtrl;
import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.Utils.AdVideoCtrl;
import com.bupt.adsystem.Utils.NewImageMgr;
import com.bupt.adsystem.Utils.NewVideoMgr;
import com.bupt.adsystem.Utils.Property;
import com.bupt.adsystem.downloadtask.DownloadManager;
import com.bupt.adsystem.model.ElevatorInfo;
import com.bupt.sensordriver.rfid;
import com.bupt.sensordriver.sensor;

import org.anyrtc.core.AnyRTMP;
import org.anyrtc.core.RTMPGuestHelper;
import org.anyrtc.core.RTMPGuestKit;
import org.anyrtc.core.RTMPHosterHelper;
import org.anyrtc.core.RTMPHosterKit;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private ImageSwitcher mImageSwitcher;
    private VideoView mVideoView;
    private TextureView mTextureView;
    private SurfaceViewRenderer mRTCSurfaceView;
    private VideoRenderer mRTCRenderer = null;
    private RTMPHosterKit mHosterKit = null;
    private RTMPGuestKit mGuestKit = null;

    private TextView mElevatorTextView;
    private String resPath;
    private CameraApp mCameraApp;
    private TelephonyManager mTelephonyManager;
    private Context mContext;
    private ServerRequest mServerRequest;
    private AdImageCtrl mAdImageCtrl;
    private AdVideoCtrl mAdVideoCtrl;

    public static final int Elevator_Info = 1;

    private ImageView logoView;
    private sensor mSensor;
    private rfid mRfid = rfid.instance();

    UVCCameraEnumerator.UVCVideoRecorder mUVCVideoRecoder;

    TextView mDateTv, mWeekTv;
    TextClock mTimeTv;
    TextView mFloorStatusTv;
    TextView mPhoneStateTv;
    ImageView mSignalIv;
    TextView mMiscInfoTv;
    TextView mNetStatusTv;

    // TODO: test
    ImageView mCameraPushBtn;   // guard logo image as test button
    ImageView mInitImageIv;

    String telNum = "13598163660";

    private Handler mMainHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            if (msg.what == Elevator_Info) {
                /*String elevatorInfo = (String) msg.obj;
                mElevatorTextView.setText(elevatorInfo);*/

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.setContentView(R.layout.activity_main);
        initView();
        mContext = this;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetStatusReceiver, mFilter);

        mRfid = rfid.instance();
        mSensor = sensor.instance();
        Property.init(getApplicationContext());


//        final String mRtmpUrl = "rtmp://192.168.1.101:1935/live/test";
//        final String mRtmpUrl = "rtmp://aokai.lymatrix.com/aokai/test25.mp4";
//        final String mPullUrl = "rtmp://aokai.lymatrix.com/aokai/test25.mp4";
        final UVCCameraEnumerator mUVCCamera = UVCCameraEnumerator.instance(getApplicationContext(), null);
        mRTCSurfaceView.init(AnyRTMP.Inst().Egl().getEglBaseContext(), null);
        mRTCRenderer = new VideoRenderer(mRTCSurfaceView);
/*        mHosterKit = new RTMPHosterKit(this, mRTMPHosterHelper);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) Log.d(TAG, "set UVCCameraCapturer()!");
                // Caution: this should be done when UVCCamera is connected!
                mHosterKit.setUVCCameraCapturer(mRTCRenderer.GetRenderPointer(), mUVCCamera);
            }
        }, 5000);*/

        final Activity activity = this;
        mCameraPushBtn.setOnClickListener(new View.OnClickListener() {
            boolean isPush = false;
            @Override
            public void onClick(View v) {
                if (!isPush) {
                    mHosterKit = new RTMPHosterKit(activity, mRTMPHosterHelper);
                    mHosterKit.setUVCCameraCapturer(mRTCRenderer.GetRenderPointer(), mUVCCamera);
                    mHosterKit.StartRtmpStream(Property.RTMP_PUSH_URL);
                    mVideoView.setVisibility(View.INVISIBLE);
                    mVideoView.setZOrderOnTop(false);
                    mRTCSurfaceView.setVisibility(View.VISIBLE);

//                    mGuestKit = new RTMPGuestKit(activity, mRTMPGuestHelper);
//                    mGuestKit.StartRtmpPlay(Property.RTMP_PULL_URL, mRTCRenderer.GetRenderPointer());

//                    mUVCVideoRecoder = mUVCCamera.getVideoRecorder();
//                    mUVCVideoRecoder.startRecord();
                } else {
                    mHosterKit.StopRtmpStream();
                    mVideoView.setVisibility(View.VISIBLE);
                    mVideoView.setZOrderOnTop(true);
                    mRTCSurfaceView.setVisibility(View.INVISIBLE);
                    mHosterKit.Clear();
                    mHosterKit = null;

//                    mGuestKit.StopRtmpPlay();
//                    mGuestKit.Clear();
//                    mGuestKit = null;
                }
                isPush = !isPush;
            }
        });


        DownloadManager.instance(getApplicationContext());
        new ServerRequest(getApplicationContext(), mMainHandler);
        NewVideoMgr.instance(getApplicationContext(), mVideoView);
        NewImageMgr.instance(getApplicationContext(), mImageSwitcher);


/*        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Time time = new Time("GMT+8");
//                time.set(System.currentTimeMillis() + 10000);
//                String alarmTime = String.format("%02d:%02d:%02d", time.hour, time.minute, time.second);
//                Log.d(TAG, alarmTime);
//                AlarmUtil.setImageChangeTimeBroadcast(mContext, alarmTime, true);
//                AlarmUtil.setVideoChangeTimeBroadcast(mContext, alarmTime, true);

//                String url = "http://117.158.178.198:8010/esmp-ly-o-websvr/ws/esmp?wsdl";
//                JSONObject jsonObject = new JSONObject();
//                Handler handler = new Handler();
//                try {
//                    jsonObject.put("deviceId", "10000000000000000001");
//                    Log.d(TAG, "Request Json Content: \n" +
//                            jsonObject.toString());
//
////                    NetUtil.postRequestTextFile(url, jsonObject.toString(), handler);
////                    NetUtil.getRequestTextFile(url+"="+jsonObject.toString(), handler);
////                    NetUtil.requestJsonFromWebservice(url, jsonObject.toString(), handler);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
////                String urlGet = NetUtil.generateHttpGetUrl(0, 1, 80, 0, 0, 1, -89);
////                NetUtil.getRequestTextFile(urlGet, handler);

                Class<TelephonyManager> c = TelephonyManager.class;
                try
                {
                    Method getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null);
                    getITelephonyMethod.setAccessible(true);
                    ITelephony iTelephony = null;
                    iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelMgr, (Object[]) null);
//                   iTelephony.endCall();
//                    iTelephony.answerRingingCall();

                    iTelephony.dial("+8618811610769");
                }
                catch (Exception e)
                {
                    Log.e(TAG, "Fail to answer ring call.", e);
                }

                if (!mTestButtonStatus) {
                    mHosterKit.StartRtmpStream(mRtmpUrl);
                    mVideoView.setVisibility(View.INVISIBLE);
                    mVideoView.setZOrderOnTop(false);
                    mRTCSurfaceView.setVisibility(View.VISIBLE);

                    mGuestKit = new RTMPGuestKit(mAct, mRTMPGuestHelper);
                    mGuestKit.StartRtmpPlay(mPullUrl, mRTCRenderer.GetRenderPointer());

                    mUVCVideoRecoder = mUVCCamera.getVideoRecorder();
                    mUVCVideoRecoder.startRecord();
                } else {
                    mHosterKit.StopRtmpStream();
                    mVideoView.setVisibility(View.VISIBLE);
                    mVideoView.setZOrderOnTop(true);
                    mRTCSurfaceView.setVisibility(View.INVISIBLE);

                    mGuestKit.StopRtmpPlay();
                    mGuestKit.Clear();
                    mGuestKit = null;


                    *//*  when this function is called,
                    *  RTMPHosterKit need to instantiate once again!
                    *//*
//                    mHosterKit.Clear();
                    mUVCVideoRecoder.stopRecord();
                }

                if (DEBUG) Log.d(TAG, "Button Pressed!");
            }
        });*/

    }

    private void initView() {

        mDateTv = (TextView) findViewById(R.id.date);
        mWeekTv = (TextView) findViewById(R.id.week);
        mTimeTv = (TextClock) findViewById(R.id.time);
        mFloorStatusTv = (TextView) findViewById(R.id.floor_tv);
        mPhoneStateTv = (TextView) findViewById(R.id.phone_state);
        mSignalIv = (ImageView) findViewById(R.id.signal_iv);

        mTextureView = (TextureView) findViewById(R.id.textureView);    // 用来展示UVCCamera视频
        mVideoView = (VideoView) findViewById(R.id.surface_view);   // 用来播放广告视频
        mRTCSurfaceView = (SurfaceViewRenderer) findViewById(R.id.webrtc_surface_view);     // 用来采集生成RMTP流
        mTextureView.setVisibility(View.INVISIBLE);
        mVideoView.setVisibility(View.INVISIBLE);
//        mVideoView.setZOrderOnTop(true);
        mRTCSurfaceView.setVisibility(View.INVISIBLE);

        mMiscInfoTv = (TextView) findViewById(R.id.misc_info_tv);
        mImageSwitcher = (ImageSwitcher) findViewById(R.id.image_switcher); // 展示广告图像
        mNetStatusTv = (TextView) findViewById(R.id.network_tv);

        mCameraPushBtn = (ImageView) findViewById(R.id.guard_logo);
        mInitImageIv = (ImageView) findViewById(R.id.image_init_view);

        updateDateAndTime();
    }

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener(){

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            // TODO Auto-generated method stub
            super.onSignalStrengthsChanged(signalStrength);

            // For Lte SignalStrength: dbm = ASU - 140.
            // For GSM Signal Strength: dbm =  (2*ASU)-113.
            String[] parts = signalStrength.toString().split(" ");
            int lteSigStrength = Integer.valueOf(parts[8]) - 140;

            mPhoneStateTv.setText(lteSigStrength + "dBm");
            mSignalIv.setImageResource(imageIdMap[getLteLevel(signalStrength)]);
            ElevatorInfo.instance().setCSignal(lteSigStrength);
        }
    };

    private BroadcastReceiver mNetStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                ConnectivityManager manager =
                        (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = manager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isAvailable()) {
                    mNetStatusTv.setText("在线运行");
                } else {
                    mNetStatusTv.setText("断网运行");
                }
            }
        }
    };


    private static final int[] imageIdMap = {
            R.mipmap.signal1,
            R.mipmap.signal1,
            R.mipmap.signal2,
            R.mipmap.signal3,
            R.mipmap.signal4,
    };

    private int getLteLevel(SignalStrength signalStrength) {
        try {
            Class classFromName = Class.forName(SignalStrength.class.getName());
            Method method = classFromName.getMethod("getLteLevel");
            Object object = method.invoke(signalStrength);
            return (int)object;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    static final String[] sWeekNameMap = {
            "empty",    // 没有星期0,填充空值
            "星期日",
            "星期一",
            "星期二",
            "星期三",
            "星期四",
            "星期五",
            "星期六"
    };

    public void updateDateAndTime() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
        String mWeek = sWeekNameMap[c.get(Calendar.DAY_OF_WEEK)];

        mDateTv.setText(mYear + "-" + mMonth + "-" + mDay);
        mWeekTv.setText(mWeek);
        mTimeTv.setFormat12Hour("hh:mm");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LifeCycleMgr.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        LifeCycleMgr.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        if (mServerRequest != null) {
            mServerRequest.httpDisconnect();
        }

        if (mNetStatusReceiver != null) {
            unregisterReceiver(mNetStatusReceiver);
        }

        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            mPhoneStateListener = null;
        }
        super.onDestroy();
    }

    private final static List<Integer> keycodeSet = Arrays.asList(
            KeyEvent.KEYCODE_A,
            KeyEvent.KEYCODE_K,
            KeyEvent.KEYCODE_L,
            KeyEvent.KEYCODE_S,
            KeyEvent.KEYCODE_ENTER
    );

    private final static String[] warnMsg = {
            "empty for inflate",
            "非困人非平层停梯报警",         // 1
            "非困人断电报警",              // 2
            "非困人蹲底报警",              // 3
            "非困人冲顶报警",              // 4
            "非平层停梯困人报警",           // 5
            "平层困人报警",                // 6
            "断电困人报警",
            "运行中开门困人报警",           // 8
            "蹲底困人报警",
            "冲顶困人报警",                // 9
            "一键拨号"                    // 10
    };

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {

        if (DEBUG) Log.d(TAG, "keycode --> " + keycode);

        Toast.makeText(this,"检测到案件按下：" + keycode, Toast.LENGTH_LONG).show();

        if (!keycodeSet.contains(keycode)) return super.onKeyDown(keycode, event);

        int[] sensorData = mSensor.getSensorData();

        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sensorData.length; i++) {
                sb.append("data[" + i + "]: " + sensorData[i] + "  ");
            }
            Log.d(TAG, sb.toString());
            mMiscInfoTv.setText(sb.toString());
        }

        mFloorStatusTv.setText(String.valueOf(sensorData[0]));
        switch (sensorData[1]) {
            case 0:
                mFloorStatusTv.setCompoundDrawablesWithIntrinsicBounds(null,
                        ContextCompat.getDrawable(this, R.mipmap.down_icon), null, null);
                break;
            case 1:
                mFloorStatusTv.setCompoundDrawablesWithIntrinsicBounds(null,
                        ContextCompat.getDrawable(this, R.mipmap.up_icon), null, null);
                break;
            case 2:
                mFloorStatusTv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                break;
        }

        if (sensorData[3] >= 1 && sensorData[3] <= 4) {
            Toast.makeText(this, warnMsg[sensorData[3]], Toast.LENGTH_LONG).show();
//            sendSMSMsg(telNum, warnMsg[sensorData[3]]);
        } else if (sensorData[3] > 4 && sensorData[3] <= 11) {
            Toast.makeText(this, warnMsg[sensorData[3]], Toast.LENGTH_LONG).show();
//            callPhone(telNum);
        }
        // TODO: 更新ElevatorInfo的相关信息, 根据底层传感器的上报信息的格式，更新相应数据，待确定
        //  ElevatorInfo.instance().setHasPerson();
        NetUtil.asyncReportElevatorInfo(ElevatorInfo.instance(), mMainHandler);
        return true;
    }

    private void callPhone(String telNum) {
        try {
            Uri uri = Uri.parse("tel:" + telNum);
            Intent call = new Intent(Intent.ACTION_CALL, uri);  // 直接播出电话
            startActivity(call);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void sendSMSMsg(String telNum, String msgContent) {
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(telNum, null, msgContent, null, null);
        Toast.makeText(this, "短信已发送", Toast.LENGTH_SHORT).show();
    }

    final RTMPHosterHelper mRTMPHosterHelper = new RTMPHosterHelper() {
        @Override
        public void OnRtmpStreamOK() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "RTMP 连接成功！");
                }
            });
        }

        @Override
        public void OnRtmpStreamReconnecting(final int times) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, String.format("RTMP 重连中(%1$d秒)...", times));
                }
            });
        }

        @Override
        public void OnRtmpStreamStatus(final int delayMs, final int netBand) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG)
                        Log.d(TAG, String.format(getString(org.anyrtc.anyrtmp.R.string.str_rtmp_status), delayMs, netBand));
                }
            });
        }

        @Override
        public void OnRtmpStreamFailed(int code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "RTMP 连接失败");
                }
            });
        }

        @Override
        public void OnRtmpStreamClosed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "RTMP 关闭!");
                }
            });
        }
    };

    final RTMPGuestHelper mRTMPGuestHelper = new RTMPGuestHelper() {
        @Override
        public void OnRtmplayerOK() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "RTMP 连接成功！");
                }
            });
        }

        @Override
        public void OnRtmplayerStatus(final int cacheTime, final int curBitrate) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG)
                        Log.d(TAG, String.format("RTMP status time: (%1$d秒), speed: (%1$d秒)", cacheTime, curBitrate));
                }
            });
        }

        @Override
        public void OnRtmplayerCache(final int time) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, String.format("RTMP 缓存时间(%1$d秒)...", time));
                }
            });
        }

        @Override
        public void OnRtmplayerClosed(int errcode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "RTMP 关闭！");
                }
            });
        }
    };
}
