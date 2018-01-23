package com.mobileapplicationdev.roboproject;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

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
                     ObjectOutputStream objectOutputStream = new ObjectOutputStream
                             (steeringSocket.getOutputStream())) {

                    ControlDataPacket controlDataPacket;

                    while (mainActivity.getToggleButtonStatus()) {
                        if (!mainActivity.getForwardButtonStatus() &&
                            !mainActivity.getBackwardButtonStatus()) {

                            controlDataPacket = new ControlDataPacket();
                            objectOutputStream.writeObject(controlDataPacket);

                        } else if (mainActivity.getForwardButtonStatus()) {
                            controlDataPacket = new ControlDataPacket
                                    (1, controlData.getDrivingMode(), controlData.getAngle());

                            objectOutputStream.writeObject(controlDataPacket);

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
