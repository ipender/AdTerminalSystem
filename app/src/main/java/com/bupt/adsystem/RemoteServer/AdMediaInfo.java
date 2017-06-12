package com.bupt.adsystem.RemoteServer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hadoop on 16-10-22.
 */
public class AdMediaInfo {

    public HashMap<String, VideoAdInfo> videoContainer = new HashMap<>();
    public HashMap<String, ImageAdInfo> imageContainer = new HashMap<>();
    public HashMap<String, OneVideoInterval> videoIntervalContainer = new HashMap<>();

    public HashMap<String, VideoAdInfo> getVideoContainer() {
        return videoContainer;
    }

    public void setVideoContainer(HashMap<String, VideoAdInfo> videoContainer) {
        this.videoContainer = videoContainer;
    }

    public HashMap<String, OneVideoInterval> getVideoIntervalContainer() {
        return videoIntervalContainer;
    }

    public void setVideoIntervalContainer(HashMap<String, OneVideoInterval> videoIntervalContainer) {
        this.videoIntervalContainer = videoIntervalContainer;
    }

    public HashMap<String, ImageAdInfo> getImageContainer() {
        return imageContainer;
    }

    public void setImageContainer(HashMap<String, ImageAdInfo> imageContainer) {
        this.imageContainer = imageContainer;
    }

    public HashMap<String, OneImageInterval> getImageIntervalContainer() {
        return imageIntervalContainer;
    }

    public void setImageIntervalContainer(HashMap<String, OneImageInterval> imageIntervalContainer) {
        this.imageIntervalContainer = imageIntervalContainer;
    }

    public HashMap<String, OneImageInterval> imageIntervalContainer = new HashMap<>();
    public String resolution;
    public String ver;
    public String templet;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Resolution: " + resolution + " Version: " + ver + " Templet: " + templet + "\n");
        Set<String> videoSet = videoContainer.keySet();
        sb.append("    VideoFile: " + videoSet.size() + " file total \n");
        for (String key : videoSet) {
            sb.append(videoContainer.get(key));
        }

        Set<String> imageSet = imageContainer.keySet();
        sb.append("    ImageFile: " + imageSet.size() + " file total \n");
        for (String key : imageSet) {
            sb.append(imageContainer.get(key));
        }

        Set<String> videoIntervalSet = videoIntervalContainer.keySet();
        sb.append("    VideoIntervals: " + videoIntervalSet.size() + " interval total \n");
        for (String key : videoIntervalSet) {
            sb.append(videoIntervalContainer.get(key));
        }

        Set<String> imageIntervalSet = imageIntervalContainer.keySet();
        sb.append("    ImageIntervals: " + imageIntervalSet.size() + " interval total \n");
        for (String key : imageIntervalSet) {
            sb.append(imageIntervalContainer.get(key));
        }
        return sb.toString();
    }

    public static HashSet<String> getHashMapKeyDifference(HashMap<String, ? extends Object> minuend,
                                                          HashMap<String, ? extends Object> subtrahend) {
        HashSet<String> hashSet = new HashSet<>();
        Set<String> keySet = minuend.keySet();

        for (String key : keySet) {
            if (!subtrahend.containsKey(key)) {
                hashSet.add(key);
            }
        }

        return hashSet;
    }

    public static Set<String> getSetDifference(final Set<String> minuend, final Set<String> subtrahend) {
        HashSet<String> hashSet = new HashSet<>(subtrahend);
        Set<String> set = new HashSet<>();

        for (String key : minuend) {
            if (!hashSet.contains(key)) {
                set.add(key);
            }
        }
        return set;
    }

    public class VideoAdInfo {
        public VideoAdInfo(){}
        String id;
        String filename;
        String md5;
        String voice;
        String b_day;
        String e_day;
        String elipse;

        @Override
        public String toString() {
            return "Id: " + id + " File: " + filename + "\n" +
                    " Md5: " + md5 + " Voice: " + voice + " PlayDuration: " + elipse + "\n" +
                    " BeginData: " + b_day + " EndData: " + e_day + "\n" ;
        }
    }

    public class ImageAdInfo {
        String id;
        String filename;
        String md5;
        String b_day;
        String e_day;
        String elipse;

        @Override
        public String toString() {
            return "ImageFile: " + "Id: " + id + " File: " + filename + " Md5: " + md5 + "\n" +
                    " BeginData: " + b_day + " EndData: " + e_day + " PlayDuration: " + elipse + "\n";
        }
    }

    public class OneVideoInterval {
        String id;
        String begin;
        String end;
        String prime_time;
        HashSet<String> fileContainer = new HashSet<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("VideoInterval: \n" + "Id: " + id + " BeginTime:" + begin + " EndTime:" + end + " Prime:" + prime_time + "\n");
            for (String fileId : fileContainer) {
                sb.append("VideoFileId:" + fileId + "\n");
            }
            return sb.toString();
        }
    }

    class OneImageInterval {
        String id;
        String begin;
        String end;
        String prime_time;
        HashSet<String> fileContainer = new HashSet<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ImageInterval: \n" + "Id: " + id + " BeginTime:" + begin + " EndTime:" + end + " Prime:" + prime_time + "\n");
            for (String fileId : fileContainer) {
                sb.append("VideoFileId:" + fileId + "\n");
            }
            return sb.toString();
        }
    }

    public static AdMediaInfo parseXmlFromText(String xmlText) {
        AdMediaInfo mediaInfo = null;
        try {
            Document doc = DocumentHelper.parseText(xmlText);
            mediaInfo = parseXml(doc);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return mediaInfo;
    }

    public static AdMediaInfo parseXml(Document doc) {
        AdMediaInfo mediaInfo = new AdMediaInfo();
        Element root = doc.getRootElement();
        mediaInfo.resolution = root.attributeValue(RESOLUTION);
        mediaInfo.ver = root.attributeValue(VERSION);
        mediaInfo.templet = root.attributeValue(TEMPLET);

        Element filesElement = root.element(TAG_ALL_FILES);
        Element mediaElement = filesElement.element(TAG_MEDIA);
        Element pictureElement = filesElement.element(TAG_PIC);

        List<Element> mediaFiles = mediaElement.elements();
        for (Element mediaFile : mediaFiles) {
            AdMediaInfo.VideoAdInfo videoInfo = mediaInfo.new VideoAdInfo();
            videoInfo.id = mediaFile.attributeValue(ID);
            videoInfo.filename = mediaFile.attributeValue(FILE);
            videoInfo.md5 = mediaFile.attributeValue(MD5);
            videoInfo.voice = mediaFile.attributeValue(VOICE);
            videoInfo.b_day = mediaFile.attributeValue(BEGIN_DAY);
            videoInfo.e_day = mediaFile.attributeValue(END_DAY);
            videoInfo.elipse = mediaFile.attributeValue(PLAY_DURATION);
            mediaInfo.videoContainer.put(videoInfo.id, videoInfo);
        }

        List<Element> imageFiles = pictureElement.elements();
        for (Element imageFile : imageFiles) {
            AdMediaInfo.ImageAdInfo imageInfo = mediaInfo.new ImageAdInfo();
            imageInfo.id = imageFile.attributeValue(ID);
            imageInfo.filename = imageFile.attributeValue(FILE);
            imageInfo.md5 = imageFile.attributeValue(MD5);
            imageInfo.b_day = imageFile.attributeValue(BEGIN_DAY);
            imageInfo.e_day = imageFile.attributeValue(END_DAY);
            imageInfo.elipse = imageFile.attributeValue(PLAY_DURATION);
            mediaInfo.imageContainer.put(imageInfo.id, imageInfo);
        }

        Element videoInterval = root.element(TAG_MEDIA_PLAY);
        List<Element> videoIntervals = videoInterval.elements();
        for (Element interval : videoIntervals) {
            AdMediaInfo.OneVideoInterval oneInterval = mediaInfo.new OneVideoInterval();
            oneInterval.id = interval.attributeValue(INTERVAL_ID);
            oneInterval.begin = interval.attributeValue(BEGIN_TIME);
            oneInterval.end = interval.attributeValue(END_TIME);
            oneInterval.prime_time = interval.attributeValue(PRIME_TIME);
            List<Element> videoIdElements = interval.elements();
            for (Element idElement : videoIdElements) {
                String videoId = idElement.attributeValue(FILE);
                oneInterval.fileContainer.add(videoId);
            }
            mediaInfo.videoIntervalContainer.put(oneInterval.id, oneInterval);
        }

        Element imageInterval = root.element(TAG_PIC_PLAY);
        List<Element> imageIntervals = imageInterval.elements();
        for (Element interval : imageIntervals) {
            AdMediaInfo.OneImageInterval oneInterval = mediaInfo.new OneImageInterval();
            oneInterval.id = interval.attributeValue(INTERVAL_ID);
            oneInterval.begin = interval.attributeValue(BEGIN_TIME);
            oneInterval.end = interval.attributeValue(END_TIME);
            oneInterval.prime_time = interval.attributeValue(PRIME_TIME);
            List<Element> videoIdElements = interval.elements();
            for (Element idElement : videoIdElements) {
                String videoId = idElement.attributeValue(FILE);
                oneInterval.fileContainer.add(videoId);
            }
            mediaInfo.imageIntervalContainer.put(oneInterval.id, oneInterval);
        }
        return mediaInfo;
    }

    private static final String RESOLUTION = "resolution";
    private static final String VERSION = "ver";
    private static final String TEMPLET = "templet";

    private static final String ID = "id";
    private static final String FILE = "file";
    private static final String MD5 = "md5";
    private static final String VOICE = "voice";
    private static final String BEGIN_DAY = "b_day";
    private static final String END_DAY = "e_day";
    private static final String PLAY_DURATION = "elipse";

    private static final String INTERVAL_ID = "ID";
    private static final String BEGIN_TIME = "begin";
    private static final String END_TIME = "end";
    private static final String PRIME_TIME = "prime_time";

    private static final String TAG_ALL_FILES = "files";
    private static final String TAG_MEDIA = "media";
    private static final String TAG_PIC = "pic";
    private static final String TAG_ONE_FILE = "file";
    private static final String TAG_MEDIA_PLAY = "media_play";
    private static final String TAG_PIC_PLAY= "pic_play";
}
