package com.microsoftBand.collectionapp;

import com.microsoft.band.sensors.UVIndexLevel;

/**
 * Created by mohamed on 07/07/2015.
 */
public class BandData {
    private float acc_x;
    private float acc_y;
    private float acc_z;
    private float gyr_x;
    private float gyr_y;
    private float gyr_z;
    private float speed;
    private float temp;
    private int heartRate;
    private UVIndexLevel uvIndexLevel;
    private String label;
    private long timeStamp;

    public BandData(float acc_x,float acc_y,float acc_z,float gyr_x,float gyr_y,float gyr_z,float speed,float temp,int heartRate,UVIndexLevel uvIndexLevel,String label,long timeStamp){
        this.acc_x =acc_x;
        this.acc_y = acc_y;
        this.acc_z = acc_z;
        this.gyr_x = gyr_x;
        this.gyr_y = gyr_y;
        this.gyr_z = gyr_z;
        this.speed = speed;
        this.temp = temp;
        this.heartRate = heartRate;
        this.label = label;
        this.timeStamp = timeStamp;
        this.uvIndexLevel =uvIndexLevel;
    }

    public float getAcc_x() {
        return acc_x;
    }

    public float getAcc_y() {
        return acc_y;
    }

    public float getAcc_z() {
        return acc_z;
    }

    public float getGyr_x() {
        return gyr_x;
    }

    public float getGyr_y() {
        return gyr_y;
    }

    public float getGyr_z() {
        return gyr_z;
    }

    public float getSpeed() {
        return speed;
    }

    public float getTemp() {
        return temp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public UVIndexLevel getUvIndexLevel() {
        return uvIndexLevel;
    }

    public String getLabel() {
        return label;
    }

    public void setAcc_x(float acc_x) {
        this.acc_x = acc_x;
    }

    public void setAcc_y(float acc_y) {
        this.acc_y = acc_y;
    }

    public void setAcc_z(float acc_z) {
        this.acc_z = acc_z;
    }

    public void setGyr_x(float gyr_x) {
        this.gyr_x = gyr_x;
    }

    public void setGyr_y(float gyr_y) {
        this.gyr_y = gyr_y;
    }

    public void setGyr_z(float gyr_z) {
        this.gyr_z = gyr_z;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public void setUvIndexLevel() {
        this.uvIndexLevel =uvIndexLevel;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
