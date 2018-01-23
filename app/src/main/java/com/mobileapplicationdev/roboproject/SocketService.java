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
import java.net.UnknownHostException;

/**
 * Created by Florian on 17.01.2018.
 * Socket Robot Control
 */

public class SocketService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private final String className = SocketService.class.getName();

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
                String ioExceptionLoggerMsg = getString(R.string.error_msg_socket_io_exception);

                try (Socket steeringSocket = new Socket(ip, port);
                     DataOutputStream dataOutputStream = new DataOutputStream
                             (steeringSocket.getOutputStream())) {

                    while (mainActivity.getToggleButtonStatus()) {
                        ControlData controlData = mainActivity.getControlData();

                        if (!mainActivity.getForwardButtonStatus() &&
                                !mainActivity.getBackwardButtonStatus()) {

                            Log.d(SocketService.class.getName(), "Stop" + controlData.toString());
                            dataOutputStream.writeBytes("0");                                           // drive
                            dataOutputStream.writeBytes("0");                                           // not used
                            dataOutputStream.writeBytes("0");                                           // angle
                            dataOutputStream.writeBytes("0");                                           // driving mode
                            dataOutputStream.writeBytes("0");                                           // platform up not used
                            dataOutputStream.writeBytes("0");                                           // platform down not used


                        } else if (mainActivity.getForwardButtonStatus()) {
                            Log.d(SocketService.class.getName(), "Forward " + controlData.toString());
                            dataOutputStream.writeBytes("1");                                           // drive
                            dataOutputStream.writeBytes("0");                                           // not used
                            dataOutputStream.writeBytes(String.valueOf(controlData.getAngle()));          // angle
                            // dataOutputStream.writeBytes(String.valueOf(controlData.getRadianAngle()));
                            dataOutputStream.writeBytes(String.valueOf(controlData.getDrivingMode()));    // driving mode
                            dataOutputStream.writeBytes("0");                                          // platform up not used
                            dataOutputStream.writeBytes("0");                                          // platform down not used

                        } else {
                            // TODO Implement
                            // TODO invert speed
                        }
                    }
                } catch (UnknownHostException e) {
                    mainActivity.hostErrorHandler();
                } catch (IOException e) {
                    Log.e(className, ioExceptionLoggerMsg);
                }

                stopSelf();
            }
        }, "robot_control.socket.thread").start();
    }

    private int reverseByteOrder(int i) {
        return (i & 0xff) << 24 | (i & 0xff00) << 8 | (i & 0xff0000) >> 8 | (i >> 24) & 0xff;
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
        void hostErrorHandler();
    }

    //                    if (!mainActivity.getForwardButtonStatus() &&
//                            !mainActivity.getBackwardButtonStatus()) {
//
//                        Log.d(SocketService.class.getName(), "Stop" + controlData.toString());
////                            dataOutputStream.writeBytes("0");                                           // drive
////                            dataOutputStream.writeBytes("0");                                           // not used
////                            dataOutputStream.writeBytes("0");                                           // angle
////                            dataOutputStream.writeBytes("0");                                           // driving mode
////                            dataOutputStream.writeBytes("0");                                           // platform up not used
////                            dataOutputStream.writeBytes("0");                                           // platform down not used
//
//
//                    } else if (mainActivity.getForwardButtonStatus()) {
//                        Log.d(SocketService.class.getName(), "Forward " + controlData.toString());
////                            dataOutputStream.writeBytes("1");                                           // drive
////                            dataOutputStream.writeBytes("0");                                           // not used
////                            dataOutputStream.writeBytes(String.valueOf(controlData.getAngle()));          // angle
////                         // dataOutputStream.writeBytes(String.valueOf(controlData.getRadianAngle()));
////                            dataOutputStream.writeBytes(String.valueOf(controlData.getDrivingMode()));    // driving mode
////                            dataOutputStream.writeBytes("0");                                          // platform up not used
////                            dataOutputStream.writeBytes("0");                                          // platform down not used
//
//                    } else {
//                        // TODO Implement
//                        // TODO invert speed
//                    }
//                }
}
