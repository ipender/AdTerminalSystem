package com.bupt.adsystem.RemoteServer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bupt.adsystem.Utils.FileDirMgr;
import com.bupt.adsystem.Utils.UpdateMedia;
import com.bupt.adsystem.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by hadoop on 16-10-24.
 * this class should used in Singleton Mode
 */
public class MediaStrategyMgr {

    static private MediaStrategyMgr sStrategyMgr = null;

    private static final String KEY_XML_TEXT = "media_strategy";
    private Context mContext;
    AdMediaInfo adMediaInfo;

    public void changeVideoContainer(HashMap<String, AdMediaInfo.VideoAdInfo> videoContainer) {
        adMediaInfo.setVideoContainer(videoContainer);
    }

    public void changeImageContainer(HashMap<String, AdMediaInfo.ImageAdInfo> imageContainer) {
        adMediaInfo.setImageContainer(imageContainer);
    }

    public String getMediaPath() {
        return mMediaPath;
    }

    public void setMediaPath(String mediaPath) {
        mMediaPath = mediaPath;
    }

    private String mMediaPath;

    private UpdateMedia mVideoUpdateMedia;

    public UpdateMedia getVideoUpdateMedia() {
        return mVideoUpdateMedia;
    }

    public void setVideoUpdateMedia(UpdateMedia videoUpdateMedia) {
        mVideoUpdateMedia = videoUpdateMedia;
    }

    public UpdateMedia getImageUpdateMedia() {
        return mImageUpdateMedia;
    }

    public void setImageUpdateMedia(UpdateMedia imageUpdateMedia) {
        mImageUpdateMedia = imageUpdateMedia;
    }

    private UpdateMedia mImageUpdateMedia;

    public static MediaStrategyMgr instance(Context context) {
        if (sStrategyMgr == null) {
            sStrategyMgr = new MediaStrategyMgr(context);
        }
        return sStrategyMgr;
    }

    public MediaStrategyMgr(Context context) {
        mContext = context;
        String xmlText = getXmlMediaStrategy();
        mMediaPath = FileDirMgr.instance().getVideoStoragePath();
        if (xmlText != null) {
            adMediaInfo = AdMediaInfo.parseXmlFromText(xmlText);
        } else {
            adMediaInfo = new AdMediaInfo();
        }
    }

    public List<String> getVideoListWithIntervalCheck() {
        String curTime = Utils.getCurrentTime();
        HashMap<String, AdMediaInfo.OneVideoInterval> alias = adMediaInfo.videoIntervalContainer;
        HashMap<String, AdMediaInfo.VideoAdInfo> videoContainer =  adMediaInfo.videoContainer;
        if (alias == null ||
                alias.size() <= 0) return getVideoList();
        Set<String> intervalIds = alias.keySet();
        for (String intervalId : intervalIds) {
            AdMediaInfo.OneVideoInterval one = alias.get(intervalId);
            if (Utils.isTimeInInterval(curTime, one.begin, one.end)) {
                List<String> videoList = new ArrayList<>();
                String videoPath = null;
                for (String fileId : one.fileContainer) {
                    videoPath = mMediaPath + videoContainer.get(fileId).filename;
                    videoList.add(videoPath);
                }
                return videoList;
            }
        }
        return getVideoList();
    }

    public List<String> getVideoList() {
        if (adMediaInfo == null) return null;
        List<String> videoList = new ArrayList<>();
        Set<String> keySets = adMediaInfo.videoContainer.keySet();
        for (String key : keySets) {
            String videoPath = mMediaPath + adMediaInfo.videoContainer.get(key).filename;
            videoList.add(videoPath);
        }
        return videoList;
    }


    public List<String> getImageListWithIntervalCheck() {
        String curTime = Utils.getCurrentTime();
        HashMap<String, AdMediaInfo.OneImageInterval> alias = adMediaInfo.imageIntervalContainer;
        HashMap<String, AdMediaInfo.ImageAdInfo> imageContainer =  adMediaInfo.imageContainer;
        if (alias == null ||
                alias.size() <= 0) return getImageList();
        Set<String> intervalIds = alias.keySet();
        for (String intervalId : intervalIds) {
            AdMediaInfo.OneImageInterval one = alias.get(intervalId);
            if (Utils.isTimeInInterval(curTime, one.begin, one.end)) {
                List<String> imageList = new ArrayList<>();
                String imagePath = null;
                for (String fileId : one.fileContainer) {
                    imagePath = mMediaPath + imageContainer.get(fileId).filename;
                    imageList.add(imagePath);
                }
                return imageList;
            }
        }
        return getImageList();
    }


    public List<String> getImageList() {
        if (adMediaInfo == null) return null;
        List<String> imageList = new ArrayList<>();
        Set<String> keySets = adMediaInfo.imageContainer.keySet();
        for (String key : keySets) {
            String imagePath = mMediaPath + adMediaInfo.imageContainer.get(key).filename;
            imageList.add(imagePath);
        }
        return imageList;
    }

    public void savaXmlMediaStrategy(String xmlText){
        SharedPreferences xmlStorage = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = xmlStorage.edit();
        editor.putString(KEY_XML_TEXT, xmlText);
        editor.commit();
    }

    public String getXmlMediaStrategy() {
        SharedPreferences xmlStorage = PreferenceManager.getDefaultSharedPreferences(mContext);
        return xmlStorage.getString(KEY_XML_TEXT, null);
    }
}
