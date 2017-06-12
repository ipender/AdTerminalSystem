package com.bupt.adsystem.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.internal.telephony.ITelephony;
import com.bupt.adsystem.Camera.CameraApp;
import com.bupt.adsystem.Camera.UVCCameraEnumerator;
import com.bupt.adsystem.R;
import com.bupt.adsystem.RemoteServer.ServerRequest;
import com.bupt.adsystem.Utils.AdImageCtrl;
import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.Utils.AdVideoCtrl;
import com.bupt.adsystem.Utils.NewImageMgr;
import com.bupt.adsystem.Utils.NewVideoMgr;
import com.bupt.adsystem.downloadtask.DownloadManager;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import org.anyrtc.core.AnyRTMP;
import org.anyrtc.core.RTMPGuestHelper;
import org.anyrtc.core.RTMPGuestKit;
import org.anyrtc.core.RTMPHosterHelper;
import org.anyrtc.core.RTMPHosterKit;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.lang.reflect.Method;
import java.util.Calendar;
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

    private Button button;
    private TextView mElevatorTextView;
    private MediaPlayer mediaPlayer;
    private TextView textView;
    private String resPath;
    private CameraApp mCameraApp;
    private TelephonyManager mTelephonyManager;
    private Context mContext;
    private ServerRequest mServerRequest;
    private AdImageCtrl mAdImageCtrl;
    private AdVideoCtrl mAdVideoCtrl;
    TelephonyManager mTelMgr;

    public static final int Elevator_Info = 1;

    private ImageView logoView;

    boolean mTestButtonStatus = false;

    UVCCameraEnumerator.UVCVideoRecorder mUVCVideoRecoder;

    private Handler mMainHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            if (msg.what == Elevator_Info) {
                String elevatorInfo = (String) msg.obj;
                mElevatorTextView.setText(elevatorInfo);
                downTv.setVisibility(View.VISIBLE);
                textView.setText(msg.arg2 + "层");
//                downTv.setText(""+msg.arg1);
                int fo = msg.arg1;
                if (fo == 0) {
                    downTv.setVisibility(View.VISIBLE);
                    upTv.setVisibility(View.INVISIBLE);
                } else if (fo == 1) {
                    upTv.setVisibility(View.VISIBLE);
                    downTv.setVisibility(View.INVISIBLE);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.setContentView(R.layout.activity_main);
        initView();
        mContext = this;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        mImageSwitcher = (ImageSwitcher) findViewById(R.id.image_switcher);
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        mRTCSurfaceView = (SurfaceViewRenderer) findViewById(R.id.webrtc_surface_view);
        mElevatorTextView = (TextView) findViewById(R.id.Sensor_TextView);
        mTextureView.setVisibility(View.INVISIBLE);
        mVideoView.setVisibility(View.INVISIBLE);
//        mVideoView.setZOrderOnTop(true);
        mRTCSurfaceView.setVisibility(View.VISIBLE);

        final Activity mAct = this;

//        final String mRtmpUrl = "rtmp://192.168.1.101:1935/live/test";
        final String mRtmpUrl = "rtmp://aokai.lymatrix.com/aokai/test25.mp4";
        final String mPullUrl = "rtmp://aokai.lymatrix.com/aokai/test25.mp4";
        final UVCCameraEnumerator mUVCCamera = UVCCameraEnumerator.instance(getApplicationContext(), null);
        mRTCSurfaceView.init(AnyRTMP.Inst().Egl().getEglBaseContext(), null);
        mRTCRenderer = new VideoRenderer(mRTCSurfaceView);
        mHosterKit = new RTMPHosterKit(this, mRTMPHosterHelper);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) Log.d(TAG, "set UVCCameraCapturer()!");
                // Caution: this should be done when UVCCamera is connected!
                mHosterKit.setUVCCameraCapturer(mRTCRenderer.GetRenderPointer(), mUVCCamera);
            }
        }, 5000);


//        DownloadManager.instance(getApplicationContext());


        button.setOnClickListener(new View.OnClickListener() {
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
////                    MiscUtil.postRequestTextFile(url, jsonObject.toString(), handler);
////                    MiscUtil.getRequestTextFile(url+"="+jsonObject.toString(), handler);
////                    MiscUtil.requestJsonFromWebservice(url, jsonObject.toString(), handler);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
////                String urlGet = MiscUtil.generateHttpGetUrl(0, 1, 80, 0, 0, 1, -89);
////                MiscUtil.getRequestTextFile(urlGet, handler);

/*                Class<TelephonyManager> c = TelephonyManager.class;
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
                }*/

                if (!mTestButtonStatus) {
                    /*mHosterKit.StartRtmpStream(mRtmpUrl);
                    mVideoView.setVisibility(View.INVISIBLE);
                    mVideoView.setZOrderOnTop(false);
                    mRTCSurfaceView.setVisibility(View.VISIBLE);*/

                    mGuestKit = new RTMPGuestKit(mAct, mRTMPGuestHelper);
                    mGuestKit.StartRtmpPlay(mPullUrl, mRTCRenderer.GetRenderPointer());

                   /* mUVCVideoRecoder = mUVCCamera.getVideoRecorder();
                    mUVCVideoRecoder.startRecord();*/
                } else {
                   /* mHosterKit.StopRtmpStream();
                    mVideoView.setVisibility(View.VISIBLE);
                    mVideoView.setZOrderOnTop(true);
                    mRTCSurfaceView.setVisibility(View.INVISIBLE);*/

                    mGuestKit.StopRtmpPlay();
                    mGuestKit.Clear();
                    mGuestKit = null;

                    /*
                    *  when this function is called,
                    *  RTMPHosterKit need to instantiate once again!
                    * */
//                    mHosterKit.Clear();
                 /*   mUVCVideoRecoder.stopRecord();*/
                }

                mTestButtonStatus = !mTestButtonStatus;
                if (DEBUG) Log.d(TAG, "Button Pressed!");
            }
        });

   /*     NewImageMgr.instance(mContext, mImageSwitcher);
        NewVideoMgr.instance(mContext, mVideoView);
        ServerRequest request = new ServerRequest(mContext, mMainHandler);
        request.setFloorTextView(textView);*/

//        mAdVideoCtrl = AdVideoCtrl.instance(mContext, mVideoView);
//        mAdImageCtrl = AdImageCtrl.instance(mContext, mImageSwitcher);
//        mServerRequest = new ServerRequest(this);
//        mCameraApp = new CameraApp(this, mTextureView);
//        AdImageCtrl.instance(this, mImageSwitcher);
//        String url = "http://192.168.1.101:8080/download/purge_piece.mp4";
//        String url2 = "http://192.168.1.101:8080/download/coherence_piece.mp4";
//        String filename = URLUtil.guessFileName(url, null, null);
//        String filename2 = URLUtil.guessFileName(url2, null, null);
//        String filepath = FileDirMgr.instance().getCameraStoragePath();
//        DownloadManager.instance(this).startDownload(url, filepath, filename,
//                new OnDownload() {
//                    @Override
//                    public void onDownloading(String url, int finished) {
//                        if (DEBUG) Log.d(TAG, "downloaded1:" + finished);
//                    }
//
//                    @Override
//                    public void onDownloadFinished(File downloadFile) {
//                        if (DEBUG) Log.d(TAG, downloadFile.getAbsolutePath());
//                    }
//                });
//        DownloadManager.instance(this).startDownload(url2, filepath, filename2,
//                new OnDownload() {
//                    @Override
//                    public void onDownloading(String url, int finished) {
//                        if (DEBUG) Log.d(TAG, "downloaded2:" + finished);
//                    }
//
//                    @Override
//                    public void onDownloadFinished(File downloadFile) {
//                        if (DEBUG) Log.d(TAG, downloadFile.getAbsolutePath());
//                    }
//                });
//        int callState = mTelephonyManager.getCallState();
//        CellLocation cellLocation = mTelephonyManager.getCellLocation();
//        cellLocation.requestLocationUpdate();
//        mAdVideoCtrl = AdVideoCtrl.instance();
//        mTextureView.setVisibility(View.INVISIBLE);
//        mVideoView.setVisibility(View.VISIBLE);
//        mAdVideoCtrl.setVideoView(mVideoView);
//        mAdVideoCtrl.startPlayView();

//        resPath = mAdVideoCtrl.getVideoByOrder();
//        mVideoView.setVideoPath(resPath);
//        mVideoView.setZOrderOnTop(true);
//        mVideoView.start();
//        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                mVideoView.start();
//            }
//        });

//        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                mVideoView.setVideoPath(mAdVideoCtrl.getVideoByOrder());
//                mVideoView.start();
//            }
//        });
//        SurfaceHolder surfaceHolder = adVideoView.getHolder();
//        surfaceHolder.setFixedSize(720, 480);
//        surfaceHolder.addCallback(this);
//        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
//        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(mUSBMonitor.ACTION_USB_PERMISSION), 0);
//        HashMap<String, UsbDevice> usbDevcieList = mUsbManager.getDeviceList();
//        if(usbDevcieList.size() == 1){
//            Toast.makeText(this, "find a USB device!", Toast.LENGTH_LONG).show();
//            Set<String> keySet = usbDevcieList.keySet();
//            for (String key : keySet)
//            mUsbManager.requestPermission(usbDevcieList.get(key), mPermissionIntent);
//        } else {
//            Toast.makeText(this, "USB device Num is:" + usbDevcieList.size(), Toast.LENGTH_LONG).show();
//        }

/*        logoView = (ImageView)findViewById(R.id.logo_image);
        logoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,DemoActivity.class);
                startActivity(i);
            }
        });
        setStringData();*/
    }

    TextView timeTv, dateTv, weekTv;
    TextView upTv, downTv;

    private void initView() {
        timeTv = (TextView) findViewById(R.id.time);
        dateTv = (TextView) findViewById(R.id.date);
        weekTv = (TextView) findViewById(R.id.week);
        upTv = (TextView) findViewById(R.id.up_tv);
        downTv = (TextView) findViewById(R.id.down_tv);
    }

    public void setStringData() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        if ("1".equals(mWay)) {
            mWay = "星期日";
        } else if ("2".equals(mWay)) {
            mWay = "星期一";
        } else if ("3".equals(mWay)) {
            mWay = "星期二";
        } else if ("4".equals(mWay)) {
            mWay = "星期三";
        } else if ("5".equals(mWay)) {
            mWay = "星期四";
        } else if ("6".equals(mWay)) {
            mWay = "星期五";
        } else if ("7".equals(mWay)) {
            mWay = "星期六";
        }
        weekTv.setText(mWay);
        dateTv.setText(mYear + "-" + mMonth + "-" + mDay);

        String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(c.get(Calendar.MINUTE));
        timeTv.setText(hour + " : " + minute);
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
//        mCameraApp.destroy();
        if (mServerRequest != null) {
            mServerRequest.httpDisconnect();
        }
        super.onDestroy();
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
