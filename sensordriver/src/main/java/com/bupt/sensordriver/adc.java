package com.bupt.sensordriver;

public class adc {
	public native int       Open();
    public native int       Close();
    public native int       Ioctl(int num, int en);
    public native int[]     Read();
}
