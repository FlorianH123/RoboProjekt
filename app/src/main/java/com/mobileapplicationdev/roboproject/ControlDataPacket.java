package com.mobileapplicationdev.roboproject;

import java.io.Serializable;

/**
 * Created by Florian on 22.01.2018.
 */

public class ControlDataPacket implements Serializable{
    private int drive;
    private int notUsed;
    private int steeringAngle;
    private int driveModus;

    private int plattform_hoch;
    private int plattform_runter;

    public ControlDataPacket() {
        this.drive = 0;
        this.notUsed = 0;
        this.steeringAngle = 0;
        this.drive = 0;
        this.driveModus = 0;
        this.plattform_hoch = 0;
        this.plattform_runter = 0;
    }

    public int getDrive() {
        return drive;
    }

    public void setDrive(int drive) {
        this.drive = drive;
    }

    public int getNotUsed() {
        return notUsed;
    }

    public void setNotUsed(int notUsed) {
        this.notUsed = notUsed;
    }

    public int getSteeringAngle() {
        return steeringAngle;
    }

    public void setSteeringAngle(int steeringAngle) {
        this.steeringAngle = steeringAngle;
    }

    public int getDriveModus() {
        return driveModus;
    }

    public void setDriveModus(int driveModus) {
        this.driveModus = driveModus;
    }

    public int getPlattform_hoch() {
        return plattform_hoch;
    }

    public void setPlattform_hoch(int plattform_hoch) {
        this.plattform_hoch = plattform_hoch;
    }

    public int getPlattform_runter() {
        return plattform_runter;
    }

    public void setPlattform_runter(int plattform_runter) {
        this.plattform_runter = plattform_runter;
    }
}
