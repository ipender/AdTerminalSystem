package com.bupt.adsystem.Camera;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.anyrtc.core.AnyRTMP;
import org.anyrtc.core.RTMPGuestHelper;
import org.anyrtc.core.RTMPGuestKit;
import org.anyrtc.core.RTMPHosterHelper;
import org.anyrtc.core.RTMPHosterKit;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by hadoop on 17-2-22.
 */
public class MediaStreamHelper {

    private volatile static MediaStreamHelper sStreamHelper;

    private Activity mActivity;
    private Context mContext;
    private SurfaceViewRenderer mRTCSurfaceView;
    private VideoRenderer mRTCRender;
    private boolean isUVCCameraReady;

    final RTMPHosterHelper mRTMPHosterHelper;
    final RTMPGuestHelper mRTMPGuestHelper;

    private RTMPHosterKit mRTMPHosterKit;
    private RTMPGuestKit mRTMPGuestKit;

    public static MediaStreamHelper instance(Activity activity, SurfaceViewRenderer surfaceViewRenderer) {
        if (sStreamHelper == null) {
            synchronized (MediaStreamHelper.class) {
                if (sStreamHelper == null) {
                    sStreamHelper = new MediaStreamHelper(activity, surfaceViewRenderer);
                }
            }
        }
        return sStreamHelper;
    }

    public MediaStreamHelper(Activity act, SurfaceViewRenderer surfaceRender) {
        if (act == null || surfaceRender == null)
            throw new NullPointerException("MediaStreamHelper have null as parameter");

        mActivity = act;
        mContext = mActivity.getApplicationContext();
        mRTCSurfaceView = surfaceRender;
        mRTCSurfaceView.init(AnyRTMP.Inst().Egl().getEglBaseContext(), null);
        mRTCRender = new VideoRenderer(mRTCSurfaceView);
        mRTMPHosterHelper = new RTMPHosterHelper() {
            @Override
            public void OnRtmpStreamOK() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "RTMP推流 连接成功！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void OnRtmpStreamReconnecting(final int times) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, String.format("RTMP推流 重连中(%1$d秒)...", times),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void OnRtmpStreamStatus(final int delayMs, final int netBand) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, String.format("RTMP推流 延迟：%1$d 网络：%2$d", delayMs, netBand),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void OnRtmpStreamFailed(int code) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, String.format("RTMP推流 连接失败"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void OnRtmpStreamClosed() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, String.format("RTMP推流 关闭！"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        mRTMPGuestHelper = new RTMPGuestHelper() {
            @Override
            public void OnRtmplayerOK() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "RTMP拉流 连接成功！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void OnRtmplayerStatus(final int cacheTime, final int curBitrate) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, String.format("RTMP拉流 时间：%02$d 速度：%2$d", cacheTime, curBitrate),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void OnRtmplayerCache(final int time) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, String.format("RTMP拉流 缓存时间：%02$d", time),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void OnRtmplayerClosed(int errcode) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, String.format("RTMP拉流 关闭！"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
    }



}
