package com.mobileapplicationdev.roboproject;

/**
 * Created by Florian on 22.01.2018.
 */

public class ControlData {
    private int speed;
    private int drivingMode;
    private int angle;
    private double radianAngle;

    public ControlData() {
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDrivingMode() {
        return drivingMode;
    }

    public void setDrivingMode(int drivingMode) {
        this.drivingMode = drivingMode;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public double getRadianAngle() {
        return radianAngle;
    }

    public void setRadianAngle(double radianAngle) {
        this.radianAngle = radianAngle;
    }

    @Override
    public String toString() {
        return "ControlData{" +
                "speed=" + speed +
                ", drivingMode=" + drivingMode +
                ", angle=" + angle +
                ", radianAngle=" + radianAngle +
                '}';
    }
}
