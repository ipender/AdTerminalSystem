package com.bupt.adsystem.RemoteServer;

/**
 * Created by hadoop on 16-8-8.
 */
public class MessagePath {

    public static final String KEY_INDEX = "INDEX";
    public static final String KEY_PARAM = "PARAM";
    public static final String KEY_INTERVAL_START = "START_INTERVAL";
    public static final String KEY_INTERVAL_END = "END_INTERVAL";
    public static final String KEY_VOLUME = "VOLUME";

    public static final class Settings {
        public static final String PREFIX = "Settings";
        public static final String VOICE_ON_TIME = "/VoiceOnTime";
        public static final String VOLUME = "/Volume";
        public static final String DEVICE_INFO = "/DeviceInfo";
    }

    public static final class VideoDownload {
        public static final String PREFIX = "AdVideo";
        public static final String DOWNLOAD = "/Download";
        public static final String DELETE = "/Delete";
        public static final String INTERVAL_ADD = "/TimeInterval";
        public static final String INTERVAL_DELETE = "/IntervalDelete";
        public static final String INTERVAL_CLEAR = "/IntervalClear";
        public static final String VIDEO_VOLUME = "/Volume";
    }

    public static final class ImageDownload {
        public static final String PREFIX = "AdImage";
        public static final String DOWNLOAD = "/Download";
        public static final String DELETE = "/Delete";
        public static final String INTERVAL_ADD = "/TimeInterval";
        public static final String INTERVAL_DELETE = "/IntervalDelete";
        public static final String INTERVAL_CLEAR = "/IntervalClear";
    }
}
