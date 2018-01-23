package com.mobileapplicationdev.roboproject;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
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
                while (mainActivity.getToggleButtonStatus()) {
                    ControlData controlData = mainActivity.getControlData();

                    if (!mainActivity.getForwardButtonStatus() &&
                            !mainActivity.getBackwardButtonStatus()) {

                        Log.d(SocketService.class.getName(), "Stop" + controlData.toString());
//                            dataOutputStream.writeBytes("0");                                           // drive
//                            dataOutputStream.writeBytes("0");                                           // not used
//                            dataOutputStream.writeBytes("0");                                           // angle
//                            dataOutputStream.writeBytes("0");                                           // driving mode
//                            dataOutputStream.writeBytes("0");                                           // platform up not used
//                            dataOutputStream.writeBytes("0");                                           // platform down not used


                    } else if (mainActivity.getForwardButtonStatus()) {
                        Log.d(SocketService.class.getName(), "Forward " + controlData.toString());
//                            dataOutputStream.writeBytes("1");                                           // drive
//                            dataOutputStream.writeBytes("0");                                           // not used
//                            dataOutputStream.writeBytes(String.valueOf(controlData.getAngle()));          // angle
//                         // dataOutputStream.writeBytes(String.valueOf(controlData.getRadianAngle()));
//                            dataOutputStream.writeBytes(String.valueOf(controlData.getDrivingMode()));    // driving mode
//                            dataOutputStream.writeBytes("0");                                          // platform up not used
//                            dataOutputStream.writeBytes("0");                                          // platform down not used

                    } else {
                        // TODO Implement
                        // TODO invert speed
//                            controlDataPacket = new ControlDataPacket
//                                    (1, controlData.getDrivingMode(), controlData.getAngle());
//
//                            objectOutputStream.writeObject(controlDataPacket);
                    }
                }
//                try (Socket steeringSocket = new Socket(ip, port);
//                     DataOutputStream dataOutputStream = new DataOutputStream
//                             (steeringSocket.getOutputStream())) {
//
//                    while (mainActivity.getToggleButtonStatus()) {
//                        if (!mainActivity.getForwardButtonStatus() &&
//                            !mainActivity.getBackwardButtonStatus()) {
//
//                            Log.d(SocketService.class.getName(), "Stop" + controlData.toString());
//                            dataOutputStream.writeBytes("0");                                           // drive
//                            dataOutputStream.writeBytes("0");                                           // not used
//                            dataOutputStream.writeBytes("0");                                           // angle
//                            dataOutputStream.writeBytes("0");                                           // driving mode
//                            dataOutputStream.writeBytes("0");                                           // platform up not used
//                            dataOutputStream.writeBytes("0");                                           // platform down not used
//
//
//                        } else if (mainActivity.getForwardButtonStatus()) {
//                            Log.d(SocketService.class.getName(), "Forward "+ controlData.toString());
//                            dataOutputStream.writeBytes("1");                                           // drive
//                            dataOutputStream.writeBytes("0");                                           // not used
//                            dataOutputStream.writeBytes(String.valueOf(controlData.getAngle()));          // angle
//                         // dataOutputStream.writeBytes(String.valueOf(controlData.getRadianAngle()));
//                            dataOutputStream.writeBytes(String.valueOf(controlData.getDrivingMode()));    // driving mode
//                            dataOutputStream.writeBytes("0");                                          // platform up not used
//                            dataOutputStream.writeBytes("0");                                          // platform down not used
//
//                        } else {
//                            // TODO Implement
//                            // TODO invert speed
////                            controlDataPacket = new ControlDataPacket
////                                    (1, controlData.getDrivingMode(), controlData.getAngle());
////
////                            objectOutputStream.writeObject(controlDataPacket);
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                stopSelf();
            }
        }, "robot_control.socket.thread").start();
    }

    private int reverseByteOrder(int i) {
        return (i&0xff)<<24 | (i&0xff00)<<8 | (i&0xff0000)>>8 | (i>>24)&0xff;
    }

    @Override
    public void onDestroy() {
        Log.i("StopService", "The service has been stopped");
        super.onDestroy();
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
