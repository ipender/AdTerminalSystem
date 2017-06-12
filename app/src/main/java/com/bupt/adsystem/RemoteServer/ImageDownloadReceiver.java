package com.bupt.adsystem.RemoteServer;

import android.content.Context;
import android.util.Log;
import android.webkit.URLUtil;

import com.bupt.adsystem.Utils.AdImageCtrl;
import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.Utils.FileDirMgr;
import com.bupt.adsystem.Utils.FileListMgr;
import com.bupt.adsystem.downloadtask.DownloadManager;
import com.bupt.adsystem.downloadtask.OnDownload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by hadoop on 16-8-15.
 */
public class ImageDownloadReceiver implements MessageTargetReceiver {
    private static final String TAG = "ImageDownloadReceiver";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private Context mContext;
    private static int mTotalTaskNum = 0;
    private static int mFinishedTaskNum = 0;
    private AdImageCtrl mImageCtrl;
    private DownloadManager mDownloadManager;
    private ImageOnDownload mOnDownload;
    private JSONObject mJSONObject;
    private JSONArray mJSONArray;

    private FileListMgr mFileListMgr;

    @Override
    public String receiveMessage(MessageContext messageContext) {
        mContext = messageContext.getContext();
        mJSONObject = messageContext.getJSONObject();
        mFileListMgr = FileListMgr.instance(mContext);
        String resPath = FileDirMgr.instance().getImageStoragePath();

        // the mImageCtrl could be null
        mImageCtrl = AdImageCtrl.getInstanceIfExists();
        // get indexPath from MessageContext or from JSONObject
        String indexPath = messageContext.getIndexPath();

        if (indexPath.equals(MessagePath.ImageDownload.PREFIX + MessagePath.ImageDownload.DOWNLOAD)) {

            mOnDownload = new ImageOnDownload();
            try {
                mJSONArray = mJSONObject.getJSONArray(MessagePath.KEY_PARAM);
                mTotalTaskNum = mJSONArray.length();
                mDownloadManager = DownloadManager.instance(mContext);
                String resName;
                String resUrl;
                for (int i = 0; i < mTotalTaskNum; i++) {
                    resUrl = mJSONArray.getString(i);
                    resName = URLUtil.guessFileName(resUrl, null, null);
                    mDownloadManager.startDownload(resUrl,
                            resPath,
                            resName,
                            mOnDownload);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (indexPath.equals(MessagePath.ImageDownload.PREFIX + MessagePath.ImageDownload.DELETE)) {

            try {
                mJSONArray = mJSONObject.getJSONArray(MessagePath.KEY_PARAM);
                int fileNum = mJSONArray.length();
                String fileName = null;
                for (int i = 0; i < fileNum; i++) {
                    fileName = mJSONArray.getString(i);
                    mFileListMgr.deleteImageFile(fileName);
                }

                // to update the play list
                if (mImageCtrl != null) {
                    mImageCtrl.updateWhenFileDelete();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (indexPath.equals(MessagePath.ImageDownload.PREFIX + MessagePath.ImageDownload.INTERVAL_ADD)) {

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
                    mFileListMgr.insertTime4Image(startInterval, endInterval, fileName, resPath + "/" + fileName);
                }

                AlarmUtil.setImageChangeTimeBroadcast(mContext, startInterval, true);
                AlarmUtil.setImageChangeTimeBroadcast(mContext, endInterval, false);

                // to update the play list
                if (mImageCtrl != null) {
                    mImageCtrl.updateWhenIntervalAddOrEdit(startInterval, endInterval);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (indexPath.equals(MessagePath.ImageDownload.PREFIX + MessagePath.ImageDownload.INTERVAL_DELETE)) {

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

                mFileListMgr.deleteImageTime(startInterval, endInterval);

                // to update the play list
                if (mImageCtrl != null) {
                    mImageCtrl.updateWhenIntervalDelete(startInterval, endInterval);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (indexPath.equals(MessagePath.ImageDownload.PREFIX + MessagePath.ImageDownload.INTERVAL_CLEAR)) {
            try {
                if (!mJSONObject.getBoolean(MessagePath.KEY_PARAM)) return null;

                mFileListMgr.clearImageTimeInterval();

                // to update the play list
                if (mImageCtrl != null) {
                    mImageCtrl.updateWhenStrategyChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    class ImageOnDownload implements OnDownload {

        @Override
        public void onDownloading(String url, int finished) {
            if (DEBUG) Log.d(TAG, "Downloading:" + finished + " : " + url);
        }

        @Override
        public void onDownloadFinished(File downloadFile) {
            String fullPath = downloadFile.getAbsolutePath();
            String imageName = fullPath.substring(fullPath.lastIndexOf('/')+1);
            if (DEBUG) Log.d(TAG, "One Task Finished:" + fullPath);
            mFileListMgr.insertImageFile(imageName, fullPath);
            mFinishedTaskNum++;
            if (mFinishedTaskNum >= mTotalTaskNum) {
                mImageCtrl.updateWhenStrategyChanged();
                mFinishedTaskNum = 0;
                mTotalTaskNum = 0;
            }
        }
    }
}
