package com.bupt.adsystem.Utils;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hadoop on 16-8-4.
 */
public class AdVideoCtrl implements UpdateMedia {

    private static final String TAG = "AdVideoCtrl";
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

    private static AdVideoCtrl sVideoCtrl;

    private Context mContext;
    private VideoView mVideoView;

    private FileListMgr mFileListMgr;
    private Cursor mVideoListCursor;            // this cursor only have one column that is video's full path

    private FileDirMgr mFolderMgr;
    private int mTotalNumOfVideo = 0;
    private int mCurrentVideoId = 0;
    private List<File> mVideoList;
    private static List<File> mNewVideoList;
    private static boolean mVideoUpdateFlag = false;
    private String mVideoAdPath = null;

    private Handler mVideoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_PLAY_VIDEO && mVideoView != null) {
                mVideoAdPath = getVideoByOrder();

                if (mVideoAdPath == null) return;

                mVideoView.setVideoPath(mVideoAdPath);
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.start();
            }
        }
    };

    public static AdVideoCtrl instance(Context context, VideoView videoView) {
        if (sVideoCtrl == null) {
            sVideoCtrl = new AdVideoCtrl(context, videoView);
        }
        return sVideoCtrl;
    }

    public static AdVideoCtrl getInstance() {
        return sVideoCtrl;
    }

    public AdVideoCtrl(Context context, VideoView videoView) {
        this.mContext = context;
        this.mVideoView = videoView;
        mFileListMgr = FileListMgr.instance(mContext);

        mVideoListCursor = getVideoListWhenStartUp();

        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setVisibility(View.VISIBLE);
        mVideoHandler.sendEmptyMessage(MSG_PLAY_VIDEO);
    }

    public Cursor getVideoListWhenStartUp() {
        String currentTime = Utils.getCurrentTime();
        String[] timeInterval = mFileListMgr.isCurrentTimeInVideoIntervel(currentTime);
        Cursor videoCursor = null;
        if (timeInterval == null) {
            videoCursor = mFileListMgr.getAllVideoFile();
        } else {
            videoCursor = mFileListMgr.getVideoInInterval(timeInterval[0], timeInterval[1]);
        }
        videoCursor.moveToFirst();
        return videoCursor;
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

    private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (DEBUG) Log.d(TAG, "Video is on prepared!");
//            mp.start();
        }
    };

    private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {
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

    public void setVideoView(VideoView videoView) {
        this.mVideoView = videoView;
    }

    public void startPlayView() {
        mVideoAdPath = sVideoCtrl.getVideoByOrder();
        if (DEBUG) Log.d(TAG, "current video file:\n" +
                mVideoAdPath);
        if (mVideoAdPath == null) return;
        mVideoView.setVideoPath(mVideoAdPath);
        mVideoView.setZOrderOnTop(true);
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.start();
    }

    public String getVideoByOrder() {

        if (mVideoListCursor == null || mVideoListCursor.getCount() == 0) return null;

        String videoPath = mVideoListCursor.getString(0);
        if (!mVideoListCursor.moveToNext()) {
            mVideoListCursor.moveToFirst();
        }
        return videoPath;
    }

    private void cursorChange(Cursor newCursor) {
        mVideoListCursor = newCursor;
        mVideoListCursor.moveToFirst();
        if (!mVideoView.isPlaying()) {
            mVideoHandler.sendEmptyMessage(MSG_PLAY_VIDEO);
        }
    }

    @Override
    public void updateWhenAlarmUp(boolean isTimeStart) {
        Cursor newVideoCursor = null;

        if (isTimeStart) {
            String[] timeInterval = mFileListMgr.isCurrentTimeInVideoIntervel(Utils.getCurrentTime());
            if (timeInterval == null) {
                newVideoCursor = mFileListMgr.getAllVideoFile();
            } else {
                newVideoCursor = mFileListMgr.getVideoInInterval(timeInterval[0], timeInterval[1]);
            }
        } else {
            newVideoCursor = mFileListMgr.getAllVideoFile();
        }

        mVideoListCursor = newVideoCursor;
    }

    @Override
    public void updateWhenIntervalAddOrEdit(@NonNull String startTime, @NonNull String endTime) {

        if (Utils.isTimeInInterval(Utils.getCurrentTime(), startTime, endTime)) {
            Cursor newCursor = mFileListMgr.getVideoInInterval(startTime, endTime);
            cursorChange(newCursor);
            if (DEBUG) Log.d(TAG, "Time is in interval");
        }
    }

    @Override
    public void updateWhenIntervalDelete(@NonNull String startTime, @NonNull String endTime) {

        if (Utils.isTimeInInterval(Utils.getCurrentTime(), startTime, endTime)) {
            Cursor newCursor = mFileListMgr.getAllVideoFile();
            cursorChange(newCursor);
        }
    }

    @Override
    public void updateWhenStrategyChanged() {
        String[] timeInterval = null;

        timeInterval = mFileListMgr.isCurrentTimeInVideoIntervel(Utils.getCurrentTime());

        if (timeInterval == null) {
            Cursor newCursor = mFileListMgr.getAllVideoFile();
            cursorChange(newCursor);
        }
    }

    @Override
    public void updateWhenFileDelete() {
        String[] timeInterval = null;
        timeInterval = mFileListMgr.isCurrentTimeInVideoIntervel(Utils.getCurrentTime());
        Cursor newCursor;

        if (timeInterval == null) {
            newCursor = mFileListMgr.getAllVideoFile();
        } else {
            newCursor = mFileListMgr.getVideoInInterval(timeInterval[0], timeInterval[1]);
        }
        cursorChange(newCursor);
    }

    @Override
    public void updateWhenDownloadFinished() {

    }
}
