package com.bupt.adsystem.RemoteServer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.Utils.UpdateMedia;
import com.bupt.adsystem.downloadtask.DownloadManager;
import com.bupt.adsystem.downloadtask.OnDownload;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by hadoop on 16-10-21.
 */
public class MediaUpdateReceiver implements MessageTargetReceiver {

    private static final String TAG = "MediaUpdateReceiver";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    Context mContext;
    private DownloadManager mVideoDldMgr;
    private DownloadManager mImageDldMgr;

    private String mScheduleId;
    private static final String fileServer = "http://117.158.178.198:8010/media/";
    private static final String xmlStrategy = "http://117.158.178.198:8010/policy/";
    private AdMediaInfo newMediaInfo;
    private AdMediaInfo oldMediaInfo;
    private MediaStrategyMgr mStrategyMgr;
    private DownloadManager mDownloadMgr;
    private int mTotalVideoTaskNum = 0;
    private int mTotalImageTaskNum = 0;
    private int mFinishedVideoNum = 0;
    private int mFinishedImageNum = 0;

    private Set<String> videoDownloadSet;
    private Set<String> videoDeleteSet;
    private Set<String> imageDownloadSet;
    private Set<String> imageDeleteSet;
    private VideoOnDownload mVideoOnDownload;
    private ImageOnDownload mImageOnDownload;
    private String mXmlText = null;

    private Handler mMediaStrategyUpdateHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            if (msg.what == MiscUtil.QUEST_FileServer_SUCCESS) {
                mXmlText = (String) msg.obj;
                newMediaInfo = AdMediaInfo.parseXmlFromText(mXmlText);
                oldMediaInfo = mStrategyMgr.adMediaInfo;    // this line can't be deleted
                process(oldMediaInfo, newMediaInfo);
                mStrategyMgr.savaXmlMediaStrategy(mXmlText);
            }
        }
    };

    @Override
    public String receiveMessage(MessageContext messageContext) {
        mContext = messageContext.getContext();
        mScheduleId = messageContext.getScheduleId();
        mStrategyMgr = MediaStrategyMgr.instance(mContext);
        mDownloadMgr = DownloadManager.instance(mContext);
        mVideoOnDownload = new VideoOnDownload();
        mImageOnDownload = new ImageOnDownload();

//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("deviceId", "10000000000000000001");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        MiscUtil.postRequestTextFile(xmlStrategy + mScheduleId + ".xml", jsonObject.toString(), mMediaStrategyUpdateHandler);
        MiscUtil.getRequestTextFile(xmlStrategy + mScheduleId + ".xml", mMediaStrategyUpdateHandler,
                MiscUtil.QUEST_FileServer_SUCCESS);
        return null;
    }

    private void process(AdMediaInfo oldMedia, AdMediaInfo newMedia) {
        UpdateMedia updateMedia = mStrategyMgr.getVideoUpdateMedia();
        Set<String> newVideoKeySet = newMedia.videoContainer.keySet();
        Set<String> oldVideoKeySet = oldMedia.videoContainer.keySet();
        videoDownloadSet = AdMediaInfo.getSetDifference(newVideoKeySet, oldVideoKeySet);
        videoDeleteSet = AdMediaInfo.getSetDifference(oldVideoKeySet, newVideoKeySet);
        Set<String> newImageKeySet = newMedia.imageContainer.keySet();
        Set<String> oldImageKeySet = oldMedia.imageContainer.keySet();
        imageDownloadSet = AdMediaInfo.getSetDifference(newImageKeySet, oldImageKeySet);
        imageDeleteSet = AdMediaInfo.getSetDifference(oldImageKeySet, newImageKeySet);
        mTotalVideoTaskNum = videoDownloadSet.size();
        mTotalImageTaskNum = imageDownloadSet.size();
        mFinishedVideoNum = 0;
        mFinishedImageNum = 0;
        String resPath = mStrategyMgr.getMediaPath();
        String resName;
        String resUrl;
        if (mTotalVideoTaskNum > 0) {
            for (String videoId : videoDownloadSet) {
                resName = newMedia.videoContainer.get(videoId).filename;
                resUrl = fileServer + resName;
                mDownloadMgr.startDownload(resUrl,
                        resPath,
                        resName,
                        mVideoOnDownload);
            }
        }

        if (mTotalImageTaskNum > 0) {
            for (String imageId : imageDownloadSet) {
                resName = newMedia.imageContainer.get(imageId).filename;
                resUrl = fileServer + resName;
                mDownloadMgr.startDownload(resUrl,
                        resPath,
                        resName,
                        mImageOnDownload);
            }
        }
    }

    class VideoOnDownload implements OnDownload {

        @Override
        public void onDownloading(String url, int finished) {
            if (DEBUG) Log.d(TAG, "Downloading: " + url + " Finished: " + finished);
        }

        @Override
        public void onDownloadFinished(File downloadFile) {
            mFinishedVideoNum++;
            if (mFinishedVideoNum >= mTotalVideoTaskNum) {
                videoDownloadFinished(oldMediaInfo, newMediaInfo);
            }
        }
    }

    class ImageOnDownload implements OnDownload {

        @Override
        public void onDownloading(String url, int finished) {
            if (DEBUG) Log.d(TAG, "Downloading: " + url + " Finished: " + finished);
        }

        @Override
        public void onDownloadFinished(File downloadFile) {
            mFinishedImageNum++;
            if (mFinishedImageNum >= mTotalImageTaskNum) {
                imageDownloadFinished(oldMediaInfo, newMediaInfo);
            }
        }
    }

    private void videoDownloadFinished(AdMediaInfo oldMedia, AdMediaInfo newMedia) {
        HashMap<String, AdMediaInfo.VideoAdInfo> videoContainer = oldMedia.videoContainer;
        mStrategyMgr.changeVideoContainer(newMedia.getVideoContainer());
        UpdateMedia videoUpdate = mStrategyMgr.getVideoUpdateMedia();
        if (videoUpdate != null) videoUpdate.updateWhenStrategyChanged();
        int deleteSize = videoDeleteSet.size();
        if (deleteSize > 0) {
            String mediaPath = mStrategyMgr.getMediaPath();
            String filePath = null;
            File file = null;
            if (videoContainer == null) return;
            for (String videoId : videoDeleteSet) {
                filePath = mediaPath + videoContainer.get(videoId).filename;
                file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    private void imageDownloadFinished(AdMediaInfo oldMedia, AdMediaInfo newMedia) {
        HashMap<String, AdMediaInfo.ImageAdInfo> imageContainer = oldMedia.imageContainer;
        mStrategyMgr.changeImageContainer(newMedia.getImageContainer());
        UpdateMedia imageUpdate = mStrategyMgr.getImageUpdateMedia();
        if (imageUpdate != null) imageUpdate.updateWhenStrategyChanged();
        int deleteSize = imageDeleteSet.size();
        if (deleteSize > 0) {
            String mediaPath = mStrategyMgr.getMediaPath();
            String filePath = null;
            File file = null;
            if (imageContainer == null) return;
            for (String imageId : imageDeleteSet) {
                if (DEBUG) Log.d(TAG, "ImageId: " + imageId);
                filePath = mediaPath + imageContainer.get(imageId).filename;
                file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

}
