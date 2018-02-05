package com.mobileapplicationdev.roboproject.models;

/**
 * Created by Florian on 22.01.2018.
 * Data Packet which contains all necessary control data
 */

public class ControlData {
    private int speed;
    private float varI;
    private float varP;
    private float regulatorFrequency;

    private float angularVelocity;
    private float x;
    private float y;

    public ControlData(){}

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public float getVarI() {
        return varI;
    }
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "ControlData{" +
                "angularVelocity=" + angularVelocity +
                ", x=" + x +
                ", y=" + y +
                '}';
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

    public float getRegulatorFrequency() {
        return regulatorFrequency;
    }

    public void setRegulatorFrequency(float regulatorFrequency) {
        this.regulatorFrequency = regulatorFrequency;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
