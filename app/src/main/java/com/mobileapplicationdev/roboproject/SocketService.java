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
 * Socket fuer normale Robo Steuerung
 */

public class SocketService extends Service {
    private boolean isConnected = false;
    private Callbacks mainActivity;
    private final IBinder mBinder = new LocalBinder();
    private ControlDataPacket controlDataPacket;

    public class LocalBinder extends Binder {
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
                             (steeringSocket.getOutputStream());) {

                    ControlDataPacket controlDataPacket;

                    while (mainActivity.getToggleButtonStatus()) {
                        if (mainActivity.getForwardButtonStatus()) {
                             controlDataPacket = new ControlDataPacket
                                     (1, controlData.getDrivingMode(), controlData.getAngle());

                             objectOutputStream.writeObject(controlDataPacket);
                        } else if (mainActivity.getBackwardButtonStatus()) {
                            // TODO invertiere Geschwindigkeit
                            controlDataPacket = new ControlDataPacket
                                    (1, controlData.getDrivingMode(), controlData.getAngle());

                            objectOutputStream.writeObject(controlDataPacket);
                        } else {
                            controlDataPacket = new ControlDataPacket();
                            objectOutputStream.writeObject(controlDataPacket);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                stopSelf();
            }
        }, "my.thread.name").start();
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity) {
        this.mainActivity = (Callbacks) activity;
    }

    public ControlDataPacket getControlDataPacket() {
        return controlDataPacket;
    }

    public void setControlDataPacket(ControlDataPacket controlDataPacket) {
        this.controlDataPacket = controlDataPacket;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    //callbacks interface for communication with service clients!
    public interface Callbacks {
        boolean getToggleButtonStatus();

        boolean getForwardButtonStatus();

        boolean getBackwardButtonStatus();

        ControlData getControlData();
    }
}
