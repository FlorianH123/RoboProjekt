package com.mobileapplicationdev.roboproject.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mobileapplicationdev.roboproject.R;
import com.mobileapplicationdev.roboproject.activities.MainActivity;
import com.mobileapplicationdev.roboproject.models.ControlData;
import com.mobileapplicationdev.roboproject.models.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static com.mobileapplicationdev.roboproject.builder.BuildRequestMessage.sendConnect;
import static com.mobileapplicationdev.roboproject.builder.BuildRequestMessage.sendGetPID;
import static com.mobileapplicationdev.roboproject.builder.BuildRequestMessage.sendSetPID;
import static com.mobileapplicationdev.roboproject.builder.BuildRequestMessage.sendSetTarget;
import static com.mobileapplicationdev.roboproject.builder.BuildRequestMessage.sendSetSpeed;
import static com.mobileapplicationdev.roboproject.utils.Utils.swap;

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

    public void openSteeringSocket(final String ip, final int port) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] controlDataArray;

                try (Socket steeringSocket = new Socket(ip, port);
                     DataOutputStream dataOutputStream = new DataOutputStream(steeringSocket.getOutputStream());
                     ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                     DataOutputStream byteWriter = new DataOutputStream(byteArrayStream)) {

                    while (mainActivity.getConnectionButtonStatus(1)) {
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
                } catch (IOException ex) {
                    exceptionHandler(MainActivity.TAG_TAB_1, ex.getMessage());
                } catch (InterruptedException ex) {
                    Log.e(className, ex.getMessage());
                }

                stopSelf();
            }
        }, "robot_control.socket.thread").start();
    }

    public void openPlottingSocket(final String ip, final int port) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int tabId = 2;
                int messageType;
                int messageSize;


                try (Socket debugSocket = new Socket(ip, port);

                     DataOutputStream dataOS = new DataOutputStream(debugSocket.getOutputStream());
                     DataInputStream dataIS = new DataInputStream(debugSocket.getInputStream())) {

                    while (mainActivity.getConnectionButtonStatus(tabId)) {
                        if (mainActivity.getDebugButtonStatus(tabId)) {
                            ControlData controlData = new ControlData();
                            controlData.setVarP(0.8f);
                            controlData.setVarI(5.35f);
                            controlData.setRegulatorFrequency(1f);
                            controlData.setSpeed(50);

                            // Send target
                            sendSetTarget(dataOS, mainActivity.getSpinnerEngine(tabId));
                            messageType = swap(dataIS.readInt());
                            Log.d("Test", "Read Set Target");
                            Log.d("Test", "MessageType: " + messageType + "\n");
                            messageSize = swap(dataIS.readInt());
                            Log.d("Test", "MessageSize: " + messageSize + "\n" );

                            if (messageType == MessageType.ERROR.getMessageType()) {
                                throw new IOException("Fehler beim setzten des Targets");
                            }

                            // Get current PID
                            sendGetPID(dataOS);
                            messageType = swap(dataIS.readInt());
                            Log.d("Test", "Read Get PID");
                            Log.d("Test", "MessageType: " + messageType + "\n");
                            messageSize = swap(dataIS.readInt());
                            Log.d("Test", "MessageSize: " + messageSize + "\n" );

                             //mainActivity.setP(dataIS.readFloat(), tabId);
                            Log.d("Test", "Read P" + swap(dataIS.readFloat()));
                            //mainActivity.setI(dataIS.readFloat(), tabId);
                            Log.d("Test", "Read I" + swap(dataIS.readFloat()));
                            //mainActivity.setD(dataIS.readFloat(), tabId);
                            Log.d("Test", "Read D" + swap(dataIS.readFloat()));
                            //TODO was soll mit diesen Werten geschehen?

                            if (messageType == MessageType.ERROR.getMessageType()) {
                                throw new IOException("Fehler beim empfangen der PID Werte");
                            }

                            // Send new PID
                            sendSetPID(dataOS, controlData);
                            messageType = swap(dataIS.readInt());
                            Log.d("Test", "Read Set PID\n");
                            Log.d("Test", "MessageType: " + messageType + "\n");
                            messageSize = swap(dataIS.readInt());
                            Log.d("Test", "MessageSize: " + messageSize + "\n" );

                            if (messageType == MessageType.ERROR.getMessageType()) {
                                throw new IOException("Fehler beim setzen der PID Werte");
                            }

                            sendSetSpeed(dataOS, controlData);
                            messageType = swap(dataIS.readInt());
                            Log.d("Test", "Read Set Speed\n");
                            Log.d("Test", "MessageType: " + messageType + "\n");
                            messageSize = swap(dataIS.readInt());
                            Log.d("Test", "MessageSize: " + messageSize + "\n" );

                            if (messageType == MessageType.ERROR.getMessageType()) {
                                throw new IOException("Fehler beim setzen der Geschwindigkeit");
                            }

                            // Connect to device
                            final ServerSocket serverSocket = new ServerSocket(0);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Socket clientSocket;
                                    int messageType;
                                    int messageSize;
                                    float[] dataMr;


                                    try {
                                        clientSocket = serverSocket.accept();
                                        DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());

                                        while (mainActivity.getDebugButtonStatus(tabId)) {
                                            messageType = swap(dataInputStream.readInt());
                                            Log.d("Test", "MessageType: " + messageType + "\n");
                                            messageSize = swap(dataInputStream.readInt());
                                            Log.d("Test", "MessageSize: " + messageSize + "\n" );

                                            messageSize = messageSize - 8;

                                            if (messageType != MessageType.ERROR.getMessageType()) {
                                                dataMr = new float[256];

                                                for (int j = 0 ; j < messageSize ; j++) {
                                                    dataMr[j] = swap(dataInputStream.readFloat());
                                                    Log.d("Test", "Geschwindigkeit" + dataMr[j] + "\n");
                                                }
                                                // TODO Geschwindigkeit in den Graph übernehemen

                                            }
                                        }

                                        clientSocket.close();
                                        serverSocket.close();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                            sendConnect(dataOS, serverSocket.getLocalPort());
                            messageType = swap(dataIS.readInt());
                            Log.d("Test", "Receive Connect\n");
                            Log.d("Test", "MessageType: " + messageType + "\n");
                            messageSize = swap(dataIS.readInt());
                            Log.d("Test", "MessageSize: " + messageSize + "\n" );

                            if (messageType == MessageType.ERROR.getMessageType()) {
                                throw new IOException("Fehler beim connecten");
                            }

                            // Receive data

                        }
                    }
                } catch (IOException ex) {
                    exceptionHandler(MainActivity.TAG_TAB_2, ex.getMessage());
                }

                stopSelf();
            }
        }, "robot_debugPlot.socket.thread").start();
    }

    public void openRotatingEngineSocket(final String ip, final int port) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                byte[] debugData;
//                final int tabId = 3;
//
//                try (Socket debugSocket = new Socket(ip, port);
//                     DataOutputStream dataOS = new DataOutputStream(debugSocket.getOutputStream());
//                     ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
//                     DataOutputStream byteWriter = new DataOutputStream(byteArrayStream)) {
//
//                    while (mainActivity.getDebugButtonStatus(tabId)) {
//
//                        Thread.sleep(50);
//                    }
//                } catch (IOException ex) {
//                    exceptionHandler(MainActivity.TAG_TAB_3, ex.getMessage());
//                } catch (InterruptedException ex) {
//                    Log.e(className, ex.getMessage());
//                }
//
//                stopSelf();
            }
        }, "robot_rotatingEngine.socket.thread").start();
    }

    // Register Activity to the service as Callbacks client
    public void registerClient(Activity activity) {
        this.mainActivity = (Callbacks) activity;
    }

    // callbacks interface for communication with main activity!
    public interface Callbacks {
        boolean getConnectionButtonStatus(int tabId);

        ControlData getControlData();

        boolean getDebugButtonStatus(int tabId);

        ToggleButton getToggleButton(String tagTab);

        int getSpinnerEngine(int tabId);

        void setP(float p, int tabId);

        int getP(int tabId);

        void setI(float i, int tabId);

        int getI(int tabId);

        void setD(float d, int tabId);

        int getD(int tabId);
    }

    private String getErrorMessage(String exceptionMessage) {
        String ioExceptionLoggerMsg =
                getString(R.string.error_msg_socket_io_exception);

        return ioExceptionLoggerMsg + " " + exceptionMessage;
    }

    private void exceptionHandler(String tagTab, String ex) {
        final ToggleButton toggleButton = mainActivity.
                getToggleButton(tagTab);
        final String errorString = getErrorMessage(ex);

        Log.e(className, errorString);

        toggleButton.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SocketService.this, errorString,
                        Toast.LENGTH_LONG).show();
                toggleButton.setChecked(false);
            }
        });
    }
}