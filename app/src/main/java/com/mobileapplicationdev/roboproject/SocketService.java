package com.mobileapplicationdev.roboproject;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Florian on 17.01.2018.
 * Socket Robot Control
 */

public class SocketService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private Callbacks mainActivity;

    class LocalBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void openSocket(final String ip, final int port) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ControlData controlData = mainActivity.getControlData();

                try (Socket steeringSocket = new Socket(ip, port);
                     DataOutputStream dataOutputStream = new DataOutputStream
                             (steeringSocket.getOutputStream())) {

                    while (mainActivity.getToggleButtonStatus()) {
                        if (!mainActivity.getForwardButtonStatus() &&
                            !mainActivity.getBackwardButtonStatus()) {

                            dataOutputStream.writeBytes("0");                                           // drive
                            dataOutputStream.writeBytes("0");                                           // not used
                            dataOutputStream.writeBytes("0");                                           // angle
                            dataOutputStream.writeBytes("0");                                           // driving mode
                            dataOutputStream.writeBytes("0");                                           // plattform hoch not used
                            dataOutputStream.writeBytes("0");                                           // platform runter not used


                        } else if (mainActivity.getForwardButtonStatus()) {
                            dataOutputStream.writeBytes("1");                                           // drive
                            dataOutputStream.writeBytes("0");                                           // not used
                            dataOutputStream.writeBytes(String.valueOf(controlData.getAngle()));          // angle
                            dataOutputStream.writeBytes(String.valueOf(controlData.getDrivingMode()));    // driving mode
                            dataOutputStream.writeBytes("0");                                          // platform up not used
                            dataOutputStream.writeBytes("0");                                          // platform down not used

                        } else {
                            // TODO Implement
                            // TODO invert speed
//                            controlDataPacket = new ControlDataPacket
//                                    (1, controlData.getDrivingMode(), controlData.getAngle());
//
//                            objectOutputStream.writeObject(controlDataPacket);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                stopSelf();
            }
        }, "robot_control.socket.thread").start();
    }

    private int reverseByteOrder(int i) {
        return (i&0xff)<<24 | (i&0xff00)<<8 | (i&0xff0000)>>8 | (i>>24)&0xff;
    }

    // Register Activity to the service as Callbacks client
    public void registerClient(Activity activity) {
        this.mainActivity = (Callbacks) activity;
    }

    // callbacks interface for communication with main activity!
    public interface Callbacks {
        boolean getToggleButtonStatus();
        boolean getForwardButtonStatus();
        boolean getBackwardButtonStatus();
        ControlData getControlData();
    }
}
