package com.mobileapplicationdev.roboproject.models;

/**
 * Created by Cedric on 20.03.2018.
 * This class contains all information about a robot profile
 */

public class RobotProfile {
    private String name;
    private String ip;
    private int portOne;
    private int portTwo;
    private int portThree;
    private float maxAngularSpeed;
    private float maxX;
    private float maxY;

    public RobotProfile (String name, String ip, int portOne, int portTwo, int portThree, float maxAngularSpeed, float maxX, float maxY) {
        this.name = name;
        this.ip = ip;
        this.portOne = portOne;
        this.portTwo = portTwo;
        this.portThree = portThree;
        this.maxAngularSpeed = maxAngularSpeed;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPortOne() {
        return portOne;
    }

    public void setPortOne(int portOne) {
        this.portOne = portOne;
    }

    public int getPortTwo() {
        return portTwo;
    }

    public void setPortTwo(int portTwo) {
        this.portTwo = portTwo;
    }

    public int getPortThree() {
        return portThree;
    }

    public void setPortThree(int portThree) {
        this.portThree = portThree;
    }

    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
