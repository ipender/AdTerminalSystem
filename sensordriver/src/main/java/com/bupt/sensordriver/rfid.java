package com.bupt.sensordriver;

public class rfid {
    static {
        System.loadLibrary("sensor-driver");
    }

    private static rfid sRfid;

    public static rfid instance() {
        if (sRfid == null) {
            sRfid = new rfid();
        }
        return sRfid;
    }

    public rfid() {
        Open();
    }

    public long getRfidId(boolean isBigEndian) {
        byte[] bytes = ReadCardNum();

        if (bytes == null) return 0;
        long id;
        if (isBigEndian) {
            id = (bytes[0] & 0xff << 24) + (bytes[1] & 0xff << 16) +
                    (bytes[2] & 0xff << 8) + bytes[3] & 0xff;
        } else {
            id = (bytes[3] & 0xff << 24) + (bytes[2] & 0xff << 16) +
                    (bytes[1] & 0xff << 8) + bytes[0] & 0xff;
        }
        return id;
    }

    public native int Open();

    public native int Close();

    public native int Ioctl(int num, int en);

    public native int[] Read();

    // represent the id card number of 4 byte
    public native byte[] ReadCardNum();
}
