package com.bupt.adsystem.RemoteServer;

/**
 * Created by hadoop on 16-8-8.
 */
public interface MessageTargetReceiver {
    String receiveMessage(MessageContext messageContext);
}
