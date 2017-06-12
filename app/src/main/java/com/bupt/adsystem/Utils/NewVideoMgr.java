package com.bupt.adsystem.Utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import com.bupt.adsystem.RemoteServer.MediaStrategyMgr;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hadoop on 16-10-24.
 * this class should be used in Singleton Mode
 * User should not use "new NewVideoMgr()" to create an instantiate
 */
public class NewVideoMgr implements UpdateMedia{

    private static final String TAG = "NewVideoMgr";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private static final HashMap<Integer, String> errorMap =
            new HashMap<Integer, String>(){{
                put(1, "MEDIA_ERROR_UNKNOWN");
                put(100, "MEDIA_ERROR_SERVER_DIED");
                put(200, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
                put(-1004, "MEDIA_ERROR_IO");
                put(-1007, "MEDIA_ERROR_MALFORMED");
                put(-1010, "MEDIA_ERROR_UNSUPPORTED");
                put(-110, "MEDIA_ERROR_TIMED_OUT");
                put(-2147483648, "MEDIA_ERROR_SYSTEM");
            }};

    private static final int MSG_PLAY_VIDEO = 0x01;

    private static NewVideoMgr sVideoMgr = null;

    private Context mContext;
    private MediaStrategyMgr mStrategyMgr;
    private VideoView mVideoView;
    private String mVideoAdPath = null;
    private List<String> mVideoList;
    private int mPosition = 0;


    public static NewVideoMgr instance(Context context, VideoView videoView) {
        if (sVideoMgr == null) {
            sVideoMgr = new NewVideoMgr(context, videoView);
        }
        return sVideoMgr;
    }

    private Handler mVideoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_PLAY_VIDEO && mVideoView != null) {
                mVideoAdPath = getVideoByOrder();
                if (mVideoAdPath == null) return;
                File video = new File(mVideoAdPath);
                if (!video.exists()) {
                    if (DEBUG) Log.e(TAG, "This Video Should Exists! : " + mVideoAdPath);
                    mVideoHandler.sendEmptyMessageDelayed(MSG_PLAY_VIDEO, 5000);
                    return;
                }
                if (DEBUG) Log.d(TAG, "Is Playing: " +  mVideoAdPath);
                mVideoView.setVideoPath(mVideoAdPath);
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.start();
            }
        }
    };

    private String getVideoByOrder() {
        if (mVideoList == null ) return null;
        int size = mVideoList.size();
        if (size <= 0) return null;
        mPosition ++;
        if (mPosition >= size) mPosition = 0;
        return mVideoList.get(mPosition);
    }

    public NewVideoMgr(Context context, VideoView videoView) {
        this.mContext = context;
        this.mVideoView = videoView;
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setVisibility(View.VISIBLE);
        mStrategyMgr = MediaStrategyMgr.instance(context);
        mStrategyMgr.setVideoUpdateMedia(this);
        mVideoList = mStrategyMgr.getVideoListWithIntervalCheck();
        mVideoHandler.sendEmptyMessage(MSG_PLAY_VIDEO);
    }

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            String toast = "VideoView got an error, Error Type: " + errorMap.get(what) +
                    "  Extra Error Type: " + errorMap.get(extra);
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            if (DEBUG) Log.d(TAG, toast);
            mVideoView.setVisibility(View.INVISIBLE);
            mVideoHandler.sendEmptyMessage(MSG_PLAY_VIDEO);
            return true;
        }
    };

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (DEBUG) Log.d(TAG, "Video is on prepared!");
//            mp.start();
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mVideoView.setVisibility(View.INVISIBLE);
            mVideoHandler.sendEmptyMessage(MSG_PLAY_VIDEO);
//            mVideoAdPath = getVideoByOrder();
//            mVideoView.setVideoPath(mVideoAdPath);
//            mVideoView.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mVideoView.setVisibility(View.VISIBLE);
//                    mVideoView.start();
//                }
//            }, 5000);
//            try {
//                mVideoAdPath = getVideoByOrder();
//                mp.setDataSource(mVideoAdPath);
//                mp.start();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    };

    @Override
    public void updateWhenAlarmUp(boolean isTimeStart) {

    }

    @Override
    public void updateWhenIntervalAddOrEdit(@NonNull String startTime, @NonNull String endTime) {

    }

    @Override
    public void updateWhenIntervalDelete(@NonNull String startTime, @NonNull String endTime) {

    }

    @Override
    public void updateWhenStrategyChanged() {
        mVideoList = mStrategyMgr.getVideoList();
        mVideoHandler.sendEmptyMessage(MSG_PLAY_VIDEO);
    }

    @Override
    public void updateWhenFileDelete() {

    }

    @Override
    public void updateWhenDownloadFinished() {

    }
}
