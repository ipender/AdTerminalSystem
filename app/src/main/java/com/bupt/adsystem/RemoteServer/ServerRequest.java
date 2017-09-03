package com.bupt.adsystem.RemoteServer;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.Utils.Property;
import com.bupt.sensordriver.rfid;
import com.bupt.sensordriver.sensor;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.util.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hadoop on 16-8-8.
 */
public class ServerRequest {
    private static final String TAG = "ServerRequest";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;
    private static final String HOST_NAME = "http://aokai.lymatrix.com";
    private String mWebServerUrl = Property.SOAP_WEB_SERVICE_URL;
    private static final String MethodName = "DeviceAdvScheduleDownRealVersion";

    private static final int MSG_REQUEST_OK = 0x01;
    private Context mContext = null;
    private HttpURLConnection mURLConnection;
    private MediaStrategyMgr mStrategyMgr;
    private Handler mMainHandler;
    private static int mReceivedDataSize = 0;

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

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                NetUtil.requestJsonFromWebservice(mWebServerUrl, MethodName, jsonStr, mWebRequestHandler);
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 10000);
/*        Timer time2 = new Timer();
        TimerTask timerTask2 = new TimerTask() {
            @Override
            public void run() {
                long rfidId = mRfid.getRfidId(true);
                int[] mSensorData = mSensor.getSensorData();
                String urlGet = NetUtil.generateHttpGetUrl(mSensorData[4], mSensorData[1], 80,
                        mSensorData[2], mSensorData[3], mSensorData[0], -89);
                NetUtil.getRequestTextFile(urlGet, mWebRequestHandler);
                String display = String.format("Floor: %02d MoveDir: %02d DoorStatus: %02d " +
                                "hasPerson: %02d Warning: %02d RFID: %08x",
                        mSensorData[0], mSensorData[1], mSensorData[2], mSensorData[3], mSensorData[4], rfidId);
                Message message = new Message();
                message.arg1 = mSensorData[1];
                message.arg2 = mSensorData[0];
                message.what = MainActivity.Elevator_Info;
                message.obj = display;
                mMainHandler.sendMessage(message);
            }
        };
        time2.scheduleAtFixedRate(timerTask2, 0, 1000);*/
    }

    final Handler mWebRequestHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
//            if (msg.what == MSG_REQUEST_OK) {
//                MessageContext messageContext = new MessageContext(mContext, mJSONObject);
//                String result = MessageDispatcher.dispatchMessage(messageContext);
//            }
            LogUtil.e("dispatchMessage--"+msg.what);
            if (msg.what == NetUtil.QUEST_FileServer_SUCCESS) {
                String jsonStr = (String) msg.obj;
                try {
                    JSONObject rootJson = new JSONObject(jsonStr);
                    JSONObject subJson = rootJson.getJSONObject("data");
//                    Toast.makeText(mContext,subJson.toString(),Toast.LENGTH_SHORT).show();
                    LogUtil.e("jsonStr-->"+jsonStr);
                    String scheduleId = subJson.getString("scheduleId");
                    LogUtil.e("scheduleId-->"+scheduleId);
                    LogUtil.e("mStrategyMgr.adMediaInfo.resolution-->"+mStrategyMgr.adMediaInfo.resolution);
                    if ( (mStrategyMgr.adMediaInfo.resolution == null) || (!scheduleId.equals(mStrategyMgr.adMediaInfo.resolution))) {
                        mStrategyMgr.adMediaInfo.resolution = scheduleId;
                        MessageTargetReceiver receiver = new MediaUpdateReceiver();
                        MessageContext message = new MessageContext(mContext, "{\"OK\":\"OK\"}");
                        message.setScheduleId(scheduleId);
                        receiver.receiveMessage(message);
                    }else {
                        mStrategyMgr.adMediaInfo.resolution = scheduleId;
                        MessageTargetReceiver receiver = new MediaUpdateReceiver();
                        MessageContext message = new MessageContext(mContext, "{\"OK\":\"OK\"}");
                        message.setScheduleId(scheduleId);
                        receiver.receiveMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (msg.what == NetUtil.QUEST_SUCCESS) {

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

}
