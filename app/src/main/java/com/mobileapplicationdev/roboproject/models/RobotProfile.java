package com.mobileapplicationdev.roboproject.models;

import android.util.Log;
import android.widget.Toast;

import com.mobileapplicationdev.roboproject.activities.SettingsActivity;

/**
 * Created by Cedric on 20.03.2018.
 * This class contains all information about a robot profile
 */

public class RobotProfile {
    private static final String TAG = "RobotProfile";
    private String name;
    private String ip;
    private int portOne;
    private int portTwo;
    private int portThree;
    private float maxAngularSpeed;
    private float maxX;
    private float maxY;
    private int id;
    private float frequenz;

    private static final String REGEX =
            "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])." +
                    "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])." +
                    "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])." +
                    "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])";

    public RobotProfile (String name, String ip, int portOne, int portTwo, int portThree, float maxAngularSpeed, float maxX, float maxY, float frequenz) {
            this.name = name;
            checkIp(ip);
            this.ip = ip;
            checkPort(portOne);
            this.portOne = portOne;
            checkPort(portTwo);
            this.portTwo = portTwo;
            checkPort(portThree);
            this.portThree = portThree;
            this.maxAngularSpeed = maxAngularSpeed;
            this.maxX = maxX;
            this.maxY = maxY;
            this.frequenz = frequenz;
    }

    public RobotProfile(){
    }

    private void checkIp(String ip){
        ip = ip.trim();
        if(!ip.matches(REGEX)){
            throw new IllegalArgumentException("Angegebene IP entspricht nicht den IP-Richtlinien");
        }
    }

    private void checkPort(int port){
        if(port < 0 || port > 65535){
            throw new IllegalArgumentException("Port=" + port + " ist kleiner als 0 oder größer als 65535");
        }
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
        checkIp(ip);
        this.ip = ip;
    }

    public int getPortOne() {
        return portOne;
    }

    public void setPortOne(int portOne) {
        checkPort(portOne);
        this.portOne = portOne;
    }

    public int getPortTwo() {
        return portTwo;
    }

    public void setPortTwo(int portTwo) {
        checkPort(portTwo);
        this.portTwo = portTwo;
    }

    public int getPortThree() {
        return portThree;
    }

    public void setPortThree(int portThree) {
        checkPort(portThree);
        this.portThree = portThree;
    }

    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getFrequenz() {
        return frequenz;
    }

    public void setFrequenz(float frequenz) {
        this.frequenz = frequenz;
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
