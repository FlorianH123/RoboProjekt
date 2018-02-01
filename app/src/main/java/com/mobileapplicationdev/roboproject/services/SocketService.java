package com.mobileapplicationdev.roboproject.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.mobileapplicationdev.roboproject.R;
import com.mobileapplicationdev.roboproject.models.ControlData;

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

    public class LocalBinder extends Binder {
        public SocketService getService() {
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
                byte[] controlDataArray;

                try (Socket steeringSocket = new Socket(ip, port);
                     DataOutputStream dataOutputStream = new DataOutputStream(steeringSocket.getOutputStream());
                     ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                     DataOutputStream byteWriter = new DataOutputStream(byteArrayStream)) {

                    while (mainActivity.getToggleButtonStatus()) {
                        ControlData controlData = mainActivity.getControlData();

                        byteWriter.writeFloat(swap(controlData.getX()));
                        byteWriter.writeFloat(swap(controlData.getY()));
                        byteWriter.writeFloat(swap(controlData.getAngularVelocity()));

                        //Log.d(className, controlData.toString());

                        controlDataArray = byteArrayStream.toByteArray();

                        dataOutputStream.write(controlDataArray);
                        byteArrayStream.reset();

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

    /**
     * Byte swap a single int value.
     *
     * @param value  Value to byte swap.
     * @return Byte swapped representation.
     */
    private int swap (int value) {
        int b1 = (value) & 0xff;
        int b2 = (value >>  8) & 0xff;
        int b3 = (value >> 16) & 0xff;
        int b4 = (value >> 24) & 0xff;

        return b1 << 24 | b2 << 16 | b3 << 8 | b4;
    }

    /**
     * Byte swap a single float value.
     *
     * @param value  Value to byte swap.
     * @return Byte swapped representation.
     */
    private float swap (float value) {
        int intValue = Float.floatToIntBits (value);
        intValue = swap (intValue);
        return Float.intBitsToFloat (intValue);
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