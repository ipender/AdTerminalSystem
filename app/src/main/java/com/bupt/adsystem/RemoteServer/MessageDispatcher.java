package com.bupt.adsystem.RemoteServer;

import android.util.Log;

import com.bupt.adsystem.Utils.AdSystemConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by hadoop on 16-8-8.
 */
public final class MessageDispatcher {
    private static final String TAG = "MessageDispatcher";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;
    private static final String INDEX_KEY = "INDEX";
    private static final String PARENT_PATH = "/";

    private static final HashMap<String, MessageTargetReceiver> MESSAGE_RECEIVERS = new HashMap<String, MessageTargetReceiver>();
    private static final NoMessageTargetReceiver NOT_FOUND_RECEIVER = new NoMessageTargetReceiver();

    public static String dispatchMessage(MessageContext messageContext) {
        JSONObject jsonObject = messageContext.getJSONObject();
        try {
            String index_key = jsonObject.getString(INDEX_KEY);
            messageContext.setIndexPath(index_key);
            getReceiver( index_key ).receiveMessage(messageContext);
        } catch (JSONException e) {
            e.printStackTrace();
            if (DEBUG) Log.d(TAG, "Didn't find \"" + INDEX_KEY + "\" in JSONObject");
        }
        return null;
    }

    private static MessageTargetReceiver getReceiver(String path) {
        synchronized (MESSAGE_RECEIVERS) {
            Set<String> keys = MESSAGE_RECEIVERS.keySet();
            for (String key : keys) {
                boolean isHit = path.startsWith(key) || path.startsWith(PARENT_PATH + key);
                if (isHit) {
                    if (DEBUG) Log.d(TAG, "dispatch success!");
                    return MESSAGE_RECEIVERS.get(key);
                }
            }
            if (DEBUG) Log.d(TAG, "dispatch failed!");
            return NOT_FOUND_RECEIVER;
        }
    }

    public static void registerAllMessageReceiver() {
        registerReceiver(MessagePath.VideoDownload.PREFIX, new VideoDownloadReceiver());
        registerReceiver(MessagePath.ImageDownload.PREFIX, new ImageDownloadReceiver());
    }

    public static void registerReceiver(String receiverKey, MessageTargetReceiver receiver) {
        synchronized (MESSAGE_RECEIVERS) {
            MESSAGE_RECEIVERS.put(receiverKey, receiver);
        }
    }

    public static void removeReceiver(String receiverKey) {
        synchronized (MESSAGE_RECEIVERS) {
            MESSAGE_RECEIVERS.remove(receiverKey);
        }
    }

    public static class NoMessageTargetReceiver implements MessageTargetReceiver {
        @Override
        public String receiveMessage(MessageContext messageContext) {
            return null;
        }
    }
}
