package com.sensor.magic.sensortest;

/**
 * Created by alexanderdubrovin on 25/10/2016.
 */

public class SensorData {
    private final boolean divider;
    private long timestamp;
    private double x;
    private double y;
    private double z;
    private double a;

    public SensorData() {
        divider = true;
    }

    public SensorData(long timestamp, double x, double y, double z) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
        divider = false;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
    public double getZ() {
        return z;
    }
    public void setZ(double z) {
        this.z = z;
    }
    public double getA() {
        return a;
    }
    public void setA(int a) {
        this.a = a;
    }

    public String toString()
    {
        if (divider)
            return "---------------";
        else
            return "x=" + Math.round(x * 10000.0) / 10000.0 + ", y=" + Math.round(y * 10000.0) / 10000.0 +
                ", z=" + Math.round(z * 10000.0) / 10000.0;
    }
}
