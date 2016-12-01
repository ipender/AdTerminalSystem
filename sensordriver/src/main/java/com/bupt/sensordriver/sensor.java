package com.bupt.sensordriver;

public class sensor {
    static {
        System.loadLibrary("sensor-driver");
    }

    private static sensor sSensor;

    public static sensor instance() {
        if (sSensor == null) {
            sSensor =  new sensor();
        }
        return sSensor;
    }

    public sensor() {
        Open();
    }

    public int[] getSensorData() {
//        return new int[5];
        int[] data = Read();
        if (data == null) return new int[5];
        return data;
    }

    public native int Open();

    public native int Close();

    public native int Ioctl(int num, int en);

    // 楼层、上下楼、开关门、是否有人、报警
    public native int[] Read();
}
