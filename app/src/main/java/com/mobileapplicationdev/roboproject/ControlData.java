package com.mobileapplicationdev.roboproject;

/**
 * Created by Florian on 22.01.2018.
 */

public class ControlData {
    private int speed;
    private int drivingMode;
    private int angle;
    private double radianAngle;
    private float varI;
    private float varP;
    private float regulatorFrequenz;
    private float angularVelocity;

    public ControlData(int speed, float varI, float varP, float regulatorFrequenz){
        this.speed = speed;
        this.varI = varI;
        this.varP = varP;
        this.regulatorFrequenz = regulatorFrequenz;
    }

    public ControlData(){

    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public float getVarI() {
        return varI;
    }

    public void setVarI(float varI) {
        this.varI = varI;
    }

    public float getVarP() {
        return varP;
    }

    public void setVarP(float varP) {
        this.varP = varP;
    }

    public float getRegulatorFrequenz() {
        return regulatorFrequenz;
    }

    public void setRegulatorFrequenz(float regulatorFrequenz) {
        this.regulatorFrequenz = regulatorFrequenz;
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


}
