package com.bupt.adsystem.RemoteServer;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.view.MainActivity;
import com.bupt.sensordriver.rfid;
import com.bupt.sensordriver.sensor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hadoop on 16-8-8.
 */
public class ServerRequest {
    private static final String TAG = "ServerRequest";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private static final String HOST_NAME = "http://10.210.12.237:8080";
    private String mWebServerUrl = "http://117.158.178.198:8010/esmp-ly-o-websvr/ws/esmp?wsdl";
    private static final String MethodName = "DeviceAdvScheduleDownRealVersion";

    private static final int MSG_REQUEST_OK = 0x01;
    private Context mContext = null;
    private JSONObject mJSONObject;
    private static int mReceivedDataSize = 0;
    private String mUsingServerUrl = HOST_NAME + "/adsystem/heart";
    private HttpURLConnection mURLConnection;
    private MediaStrategyMgr mStrategyMgr;
    private Handler mMainHandler;

    final rfid mRfid = rfid.instance();
    final sensor mSensor = sensor.instance();
    private int[] mSensorData;
    /* this is for test */
    private TextView mFloorView;

    public void setFloorTextView(TextView textView) {
        mFloorView = textView;
    }
    /* this is for test */

    public ServerRequest(Context context, Handler mainHandler) {
        this.mContext = context;
        mMainHandler = mainHandler;
        // Register HashMap for Route Control
        mStrategyMgr = MediaStrategyMgr.instance(mContext);
        MessageDispatcher.registerAllMessageReceiver();
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceId", "10000000000000000001");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String jsonStr = jsonObject.toString();
//        try {
//            URL url = null;
//            url = new URL(mUsingServerUrl);
//            mURLConnection = (HttpURLConnection) url.openConnection();
//            mURLConnection.setConnectTimeout(3000);
//            mURLConnection.setRequestMethod("POST");
////            mURLConnection.setRequestProperty("Connection", "Keep-Alive");
////            mURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
////            mURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; X11)");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                MiscUtil.requestJsonFromWebservice(mWebServerUrl, MethodName, jsonStr, mWebRequestHandler);
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 10000);
        Timer time2 = new Timer();
        TimerTask timerTask2 = new TimerTask() {
            @Override
            public void run() {
                long rfidId = mRfid.getRfidId(true);
                int[] mSensorData = mSensor.getSensorData();
                String urlGet = MiscUtil.generateHttpGetUrl(mSensorData[4], mSensorData[1], 80,
                        mSensorData[2], mSensorData[3], mSensorData[0], -89);
                MiscUtil.getRequestTextFile(urlGet, mWebRequestHandler);
                String display = String.format("Floor: %02d MoveDir: %02d DoorStatus: %02d " +
                                "hasPerson: %02d Warning: %02d RFID: %08x",
                        mSensorData[0], mSensorData[1], mSensorData[2], mSensorData[3], mSensorData[4], rfidId);
                Message message = new Message();
                message.what = MainActivity.Elevator_Info;
                message.obj = display;
                mMainHandler.sendMessage(message);
            }
        };
        time2.scheduleAtFixedRate(timerTask2, 0, 3000);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String urlPath = "http://10.210.12.237:8080/download/videolist.json";
//                    URL url = new URL(urlPath);
//                    HttpURLConnection mURLConnection = (HttpURLConnection) url.openConnection();
//                    mURLConnection.setConnectTimeout(3000);
//                    mURLConnection.setRequestMethod("GET");
//                    mURLConnection.setRequestProperty("Connection", "Keep-Alive");
//                    if (DEBUG) Log.d(TAG, "Send Request!");
//                    if (mURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                        InputStream inputStream = mURLConnection.getInputStream();
//                        String json = read(inputStream).toString();
//                        if (DEBUG) Log.d(TAG, "get Response:\n" +
//                                "ReceivedSize:" + mReceivedDataSize + "\n" +
//                                "Byte Size:" + json.length() + "\n" +
//                                json);
//                        mURLConnection.disconnect();
//                        try {
//                            mJSONObject = new JSONObject(json);
//                            mWebRequestHandler.sendMessage(mWebRequestHandler.obtainMessage(MSG_REQUEST_OK, mJSONObject));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            if (DEBUG) Log.d(TAG, "JSON File Format Error!");
//                        }
//                        mURLConnection.disconnect();
//                    }
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    final Handler mWebRequestHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
//            if (msg.what == MSG_REQUEST_OK) {
//                MessageContext messageContext = new MessageContext(mContext, mJSONObject);
//                String result = MessageDispatcher.dispatchMessage(messageContext);
//            }
            if (msg.what == MiscUtil.QUEST_FileServer_SUCCESS) {
                String jsonStr = (String) msg.obj;
                try {
                    JSONObject rootJson = new JSONObject(jsonStr);
                    JSONObject subJson = rootJson.getJSONObject("data");
                    String scheduleId = subJson.getString("scheduleId");
                    if ( (mStrategyMgr.adMediaInfo.resolution == null) || (!scheduleId.equals(mStrategyMgr.adMediaInfo.resolution))) {
                        mStrategyMgr.adMediaInfo.resolution = scheduleId;
                        MessageTargetReceiver receiver = new MediaUpdateReceiver();
                        MessageContext message = new MessageContext(mContext, "{\"OK\":\"OK\"}");
                        message.setScheduleId(scheduleId);
                        receiver.receiveMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (msg.what == MiscUtil.QUEST_SUCCESS) {
                /* this is for test */
                if (mFloorView == null) {
                    mFloorView.setText(mSensorData[0] + "楼");
                }
            }
        }
    };

    /**
     * 读取流中的数据
     */
    public static StringBuilder read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        mReceivedDataSize = 0;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
            mReceivedDataSize += line.getBytes().length;
        }
        return stringBuilder;
    }

    public void httpDisconnect() {
        if (mURLConnection != null) {
            mURLConnection.disconnect();
        }
    }

    public void longLiveHeart() {
        try {
            URL url = null;
            url = new URL(mUsingServerUrl);
            mURLConnection = (HttpURLConnection) url.openConnection();
            mURLConnection.setConnectTimeout(3000);
            mURLConnection.setRequestMethod("GET");


            if (DEBUG) Log.d(TAG, "Send Request!");
            if (mURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = mURLConnection.getInputStream();
                String json = read(inputStream).toString();
                if (DEBUG) Log.d(TAG, "get Response:\n" +
                        "ReceivedSize:" + mReceivedDataSize + "\n" +
                        "Byte Size:" + json.length() + "\n" +
                        json);
                mURLConnection.disconnect();
                try {
                    mJSONObject = new JSONObject(json);
                    mWebRequestHandler.sendMessage(mWebRequestHandler.obtainMessage(MSG_REQUEST_OK, mJSONObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (DEBUG) Log.d(TAG, "JSON File Format Error!");
                }
            } else {
                mURLConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
