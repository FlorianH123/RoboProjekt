package com.mobileapplicationdev.roboproject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

/**
 * Created by Florian on 17.01.2018.
 */

public class SocketService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private ControlDataPacket controlDataPacket;
    class LocalBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void openSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket steeringSocket = new Socket();


                stopSelf();
            }
        }, "my.thread.name").start();
    }

    public ControlDataPacket getControlDataPacket() {
        return controlDataPacket;
    }

    public void setControlDataPacket(ControlDataPacket controlDataPacket) {
        this.controlDataPacket = controlDataPacket;
    }
}
