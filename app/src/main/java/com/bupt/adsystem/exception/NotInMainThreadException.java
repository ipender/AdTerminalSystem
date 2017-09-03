package com.bupt.adsystem.exception;

/**
 * Created by hadoop on 17-9-3.
 */
public class NotInMainThreadException extends RuntimeException {

    public NotInMainThreadException() {
        super("this method should run in main thread!");
    }
}
