package com.bupt.adsystem.RemoteServer;

import android.content.Context;
import android.util.Log;
import android.webkit.URLUtil;

import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.Utils.AdVideoCtrl;
import com.bupt.adsystem.Utils.FileDirMgr;
import com.bupt.adsystem.Utils.FileListMgr;
import com.bupt.adsystem.downloadtask.DownloadManager;
import com.bupt.adsystem.downloadtask.OnDownload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by hadoop on 16-8-13.
 */
public class VideoDownloadReceiver implements MessageTargetReceiver {

    private static final String TAG = "VideoDownloadReceiver";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private static int mDownloadTaskNum = 0;
    private static int mFinishedTaskNum = 0;

    //    private VideoDownloadViewHolder mDownloadViewHolder;
    private Context mContext;
    private FileListMgr mFileListMgr;
    private AdVideoCtrl mAdVideoCtrl;
    private DownloadManager mDownloadManager;
    private VideoOnDownload mOnDownload;
    private JSONObject mJSONObject;
    private JSONArray mJSONArray;

//    private List<File> mDownloadedFile = new ArrayList<>();
//    private HashMap<String, String> mToDownloadList = new HashMap<String, String>();

    @Override
    public String receiveMessage(MessageContext messageContext) {

        mContext = messageContext.getContext();
        mFileListMgr = FileListMgr.instance(mContext);
        mAdVideoCtrl = AdVideoCtrl.getInstance();
        mJSONObject = messageContext.getJSONObject();
        String resPath = FileDirMgr.instance().getCameraStoragePath();

        // get indexPath from MessageContext or from JSONObject
        String indexPath = messageContext.getIndexPath();

        if (indexPath.equals(MessagePath.VideoDownload.PREFIX + MessagePath.VideoDownload.DOWNLOAD)) {
            // json parser here
            try {
                mJSONArray = mJSONObject.getJSONArray(MessagePath.KEY_PARAM);
                mDownloadManager = DownloadManager.instance(mContext);
                int downloadTaskNum = mJSONArray.length();
                mOnDownload = new VideoOnDownload(downloadTaskNum);

                for (int i = 0; i < downloadTaskNum; i++) {
                    String resUrl = mJSONArray.getString(i);
                    String resName = URLUtil.guessFileName(resUrl, null, null);
                    mDownloadManager.startDownload(resUrl,
                            resPath,
                            resName,
                            mOnDownload);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (indexPath.equals(MessagePath.VideoDownload.PREFIX + MessagePath.VideoDownload.DELETE)) {

            try {
                mJSONArray = mJSONObject.getJSONArray(MessagePath.KEY_PARAM);
                int fileNum = mJSONArray.length();
                String fileName = null;
                for (int i = 0; i < fileNum; i++) {
                    fileName = mJSONArray.getString(i);
                    mFileListMgr.deleteVideoFile(fileName);
                }

                // to update the play list
                if (mAdVideoCtrl != null) {
                    mAdVideoCtrl.updateWhenFileDelete();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (indexPath.equals(MessagePath.VideoDownload.PREFIX + MessagePath.VideoDownload.INTERVAL_ADD)) {

            try {
                String startInterval = mJSONObject.getString(MessagePath.KEY_INTERVAL_START);
                String endInterval = mJSONObject.getString(MessagePath.KEY_INTERVAL_END);
                mJSONArray = mJSONObject.getJSONArray(MessagePath.KEY_PARAM);
                int fileNum = mJSONArray.length();
                String fileName = null;

                if (startInterval.length() != 8 || endInterval.length() != 8 || endInterval.compareTo(startInterval) < 0) {
                    /**
                     * 返回服务器时间设置错误的信息
                     */

                    // 在这回复服务器
                    return null;
                }

                for (int i = 0; i < fileNum; i++) {
                    fileName = mJSONArray.getString(i);
                    mFileListMgr.insertTime4Video(startInterval, endInterval, fileName, resPath + "/" + fileName);
                }

                AlarmUtil.setVideoChangeTimeBroadcast(mContext, startInterval, true);
                AlarmUtil.setVideoChangeTimeBroadcast(mContext, endInterval, false);

                // to update the play list
                if (mAdVideoCtrl != null) {
                    mAdVideoCtrl.updateWhenIntervalAddOrEdit(startInterval, endInterval);
                }

                if (DEBUG) Log.d(TAG, "Video Operation: INTERVAL_ADD");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (indexPath.equals(MessagePath.VideoDownload.PREFIX + MessagePath.VideoDownload.INTERVAL_DELETE)) {

            try {
                JSONObject jsonObject = mJSONObject.getJSONObject(MessagePath.KEY_PARAM);
                String startInterval = jsonObject.getString(MessagePath.KEY_INTERVAL_START);
                String endInterval = jsonObject.getString(MessagePath.KEY_INTERVAL_END);

                if (startInterval.length() != 8 || endInterval.length() != 8 || endInterval.compareTo(startInterval) < 0) {
                    /**
                     * 返回服务器时间设置错误的信息
                     */

                    // 在这回复服务器
                    return null;
                }

                mFileListMgr.deleteVideoTime(startInterval, endInterval);

                // to update the play list
                if (mAdVideoCtrl != null) {
                    mAdVideoCtrl.updateWhenIntervalDelete(startInterval, endInterval);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (indexPath.equals(MessagePath.VideoDownload.PREFIX + MessagePath.VideoDownload.INTERVAL_CLEAR)) {

            try {
                if (!mJSONObject.getBoolean(MessagePath.KEY_PARAM)) return null;

                mFileListMgr.clearVideoTimeInterval();

                // to update the play list
                if (mAdVideoCtrl != null) {
                    mAdVideoCtrl.updateWhenStrategyChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (indexPath.equals(MessagePath.VideoDownload.PREFIX + MessagePath.VideoDownload.VIDEO_VOLUME)) {

        }

        return null;
    }

    private class VideoOnDownload implements OnDownload {

        public VideoOnDownload() {
        }

        public VideoOnDownload(final int dlTaskNum) {
            mDownloadTaskNum = dlTaskNum;
        }

        @Override
        public void onDownloading(String url, int finished) {
            if (DEBUG) Log.d(TAG, "Downloading:" + finished + " : " + url);
        }

        @Override
        public void onDownloadFinished(File downloadFile) {
            String fullPath = downloadFile.getAbsolutePath();
            String videoName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
            if (DEBUG) Log.d(TAG, "One Task Finished:" + downloadFile.getAbsolutePath());
            mFileListMgr.insertVideoFile(videoName, fullPath, -1);
//            mDownloadedFile.add(downloadFile);
            mFinishedTaskNum++;
            if (mFinishedTaskNum == mDownloadTaskNum) {
                if (mAdVideoCtrl != null) {
                    mAdVideoCtrl.updateWhenStrategyChanged();
                }
                mFinishedTaskNum = 0;
                mDownloadTaskNum = 0;
            }
        }
    }

}
