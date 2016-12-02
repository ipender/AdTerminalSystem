package com.bupt.adsystem.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bupt.adsystem.R;
import com.bupt.adsystem.yasea.TestActivity;
import com.bupt.adsystem.yasea.ViewActivity;


/**
 * Created by xf on 2016/12/1.
 */

public class DemoActivity extends Activity{

    Button btn1,btn2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        /**
         * 初始化RTMPC引擎
         */
//        RTMPCHybird.Inst().Init(getApplicationContext());
//        RTMPCHybird.Inst().InitEngineWithAnyrtcInfo("teameetingtest", "meetingtest", "OPJXF3xnMqW+7MMTA4tRsZd6L41gnvrPcI25h9JCA4M", "c4cd1ab6c34ada58e622e75e41b46d6d");
        btn1 = (Button)findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLiveShow();
            }
        });
        btn2 = (Button)findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startLive();
            }
        });
    }

    private void startLive(){
//        Intent it = new Intent(this, GuestActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putString("hls_url", "");
//        bundle.putString("rtmp_url", "rtmp://aokai.lymatrix.com/aokai/test25.mp4");
//        bundle.putString("anyrtcId", "1");
//        bundle.putString("userData", new JSONObject().toString());
//        bundle.putString("topic", "");
//        it.putExtras(bundle);
//        startActivity(it);
    }

    private void startLiveShow(){
//        Bundle bundle = new Bundle();
//        String rtmpPushUrl = "rtmp://aokai.lymatrix.com/aokai/test25";
//        bundle.putString("hosterId", "hostID");
//        bundle.putString("rtmp_url", rtmpPushUrl);
//        bundle.putString("hls_url", "");
//        bundle.putString("topic", "");
//        bundle.putString("andyrtcId", "1");
//        bundle.putString("video_mode", RTMPCHosterKit.RTMPVideoMode.RTMPC_Video_SD.toString());
//        bundle.putString("userData", new JSONObject().toString());
        Intent intent = new Intent(this, ViewActivity.class);
//        intent.putExtras(bundle);
        startActivity(intent);
    }
}
