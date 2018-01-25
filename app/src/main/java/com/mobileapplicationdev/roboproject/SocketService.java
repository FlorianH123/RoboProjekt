package com.mobileapplicationdev.roboproject;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Florian on 17.01.2018.
 * Socket Robot Control
 */

public class SocketService extends Service {
    private static final long SOCKET_SLEEP_MILLIS = 40;

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

                    // while connection toggle button is activated send data
                    while (mainActivity.getToggleButtonStatus()) {
                        ControlData controlData = mainActivity.getControlData();

                        dataOutputStream.writeInt(reverseByteOrder(1));                          // drive
                        dataOutputStream.writeInt(0);                                           // not used
                        dataOutputStream.writeInt(reverseByteOrder(controlData.getAngle()));       // angle
                        dataOutputStream.writeInt(reverseByteOrder(controlData.getDrivingMode())); // driving mode
                        dataOutputStream.writeInt(0);                                           // platform up not used
                        dataOutputStream.writeInt(0);                                           // platform down not used

                        Thread.sleep(SOCKET_SLEEP_MILLIS);
                    }
                } catch (UnknownHostException ex) {
                    mainActivity.hostErrorHandler();
                } catch (IOException ex) {
                    Log.e(className, ioExceptionLoggerMsg + ex.getMessage());
                } catch (InterruptedException ex) {
                    Log.e(className, ex.getMessage());
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
        ControlData getControlData();
        void hostErrorHandler();
    }
}