package com.mobileapplicationdev.roboproject;

/**
 * Created by Florian on 22.01.2018.
 */

public class ControlData {
    private int speed;
    private int drivingMode;
    private int angle;

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
}
