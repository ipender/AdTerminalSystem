package com.bupt.sensordriver;

public class led {
    static {
        System.loadLibrary("sensor-driver");
    }
	public native int       Open();
    public native int       Close();
    public native int       Ioctl(int num, int en);
    //public native int[] 	Read();
}