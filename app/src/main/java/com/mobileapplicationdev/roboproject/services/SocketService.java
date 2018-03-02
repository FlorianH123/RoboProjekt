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
import java.net.Socket;

import static com.mobileapplicationdev.roboproject.builder.BuildRequestMessage.sendConnect;
import static com.mobileapplicationdev.roboproject.builder.BuildRequestMessage.sendGetPID;
import static com.mobileapplicationdev.roboproject.builder.BuildRequestMessage.sendSetPID;
import static com.mobileapplicationdev.roboproject.builder.BuildRequestMessage.sendSetTarget;
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
                int messageType = -2;
                int messageSize = 0;
                float p;
                float i;
                float d;
                float[] dataMr;

                try (Socket debugSocket = new Socket(ip, port);
                     DataOutputStream dataOS = new DataOutputStream(debugSocket.getOutputStream());
                     DataInputStream dataIS = new DataInputStream(debugSocket.getInputStream())) {

                    while (mainActivity.getConnectionButtonTab2Status()) {
                        if (mainActivity.getDebugButtonStatus()) {
                            ControlData controlData = mainActivity.getDebugControlData();

                            // Send target
                            sendSetTarget(dataOS, controlData);
                            messageType = dataIS.readInt();

                            if (messageType == MessageType.ERROR.getMessageType()) {
                                throw new IOException("Fehler beim setzten des Targets");
                            }

                            sendGetPID(dataOS);
                            messageType = dataIS.readInt();
                            messageSize = dataIS.readInt();
                            p = dataIS.readFloat();
                            i = dataIS.readFloat();
                            d = dataIS.readFloat();
                            //TODO was soll mit diesen Werten geschehen?

                            if (messageType == MessageType.ERROR.getMessageType()) {
                                throw new IOException("Fehler beim empfangen der PID Werte");
                            }

                            sendSetPID(dataOS, controlData);
                            messageType = dataIS.readInt();
                            messageSize = dataIS.readInt();

                            if (messageType == MessageType.ERROR.getMessageType()) {
                                throw new IOException("Fehler beim setzen der PID Werte");
                            }

                            sendConnect(dataOS, port);
                            messageType = dataIS.readInt();
                            messageSize = dataIS.readInt();

                            if (messageType == MessageType.ERROR.getMessageType()) {
                                throw new IOException("Fehler beim connecten");
                            }

                            while (mainActivity.getDebugButtonStatus()) {
                                messageType = dataIS.readInt();
                                messageSize = dataIS.readInt();

                                messageSize = messageSize - 2;

                                if (messageType != MessageType.ERROR.getMessageType()) {
                                    dataMr = new float[256];

                                    for (int j = 0 ; i < messageSize ; i++) {
                                        dataMr[j] = dataIS.readFloat();
                                    }
                                    // TODO Geschwindigkeit in den Graph übernehemen
                                }
                            }

                            Thread.sleep(50);
                        }
                    }
                } catch (IOException ex) {
                    exceptionHandler(MainActivity.TAG_TAB_2, ex.getMessage());
                } catch (InterruptedException ex) {
                    Log.e(className, ex.getMessage());
                }

                stopSelf();
            }
        }, "robot_debugPlot.socket.thread").start();
    }


//    public void openPlottingSocket(final String ip, final int port) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                byte[] debugData;
//
//                try (Socket debugSocket = new Socket(ip, port);
//                     DataOutputStream dataOS = new DataOutputStream(debugSocket.getOutputStream());
//                     ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
//                     DataOutputStream byteWriter = new DataOutputStream(byteArrayStream)) {
//
//                    // TODO: 05.02.2018 send zero if no input
//
//                    while (mainActivity.getConnectionButtonTab2Status()) {
//                        if (mainActivity.getDebugButtonStatus()) {
//                            //set debug configurations
//                            ControlData controlData = mainActivity.getDebugControlData();
//                            byteWriter.writeInt(swap(controlData.getSpeed()));
//                            byteWriter.writeFloat(swap(controlData.getVarI()));
//                            byteWriter.writeFloat(swap(controlData.getVarP()));
//                            byteWriter.writeFloat(swap(controlData.getRegulatorFrequency()));
//                            byteWriter.writeInt(swap(mainActivity.getSpinnerEngine()));
//
//                            debugData = byteArrayStream.toByteArray();
//                            byteArrayStream.reset();
//                            dataOS.write(debugData);
//
//                            Thread.sleep(50);
//                        }else {
//                            //set default configurations
//                            //speed
//                            byteWriter.writeInt(swap(0));
//                            //var I
//                            byteWriter.writeFloat(swap(0));
//                            //varP
//                            byteWriter.writeFloat(swap(0));
//                            //regulator frequency
//                            byteWriter.writeFloat(swap(0));
//                            //engine
//                            byteWriter.writeInt(swap(0));
//                            debugData = byteArrayStream.toByteArray();
//                            byteArrayStream.reset();
//                            dataOS.write(debugData);
//
//                            Thread.sleep(50);
//                        }
//                    }
//                } catch (IOException ex) {
//                    exceptionHandler(MainActivity.TAG_TAB_2, ex.getMessage());
//                } catch (InterruptedException ex) {
//                    Log.e(className, ex.getMessage());
//                }
//
//                stopSelf();
//
//            }
//        }, "robot_debugPlot.socket.thread").start();
//    }
//
//    public void openRotatingEngineSocket(final String ip, final int port) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                byte[] debugData;
//
//                try (Socket debugSocket = new Socket(ip, port);
//                     DataOutputStream dataOS = new DataOutputStream(debugSocket.getOutputStream());
//                     ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
//                     DataOutputStream byteWriter = new DataOutputStream(byteArrayStream)) {
//
//                    while (mainActivity.getDebugButtonStatus()) {
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
//
//            }
//        }, "robot_rotatingEngine.socket.thread").start();
//    }

    // Register Activity to the service as Callbacks client
    public void registerClient(Activity activity) {
        this.mainActivity = (Callbacks) activity;
    }

    // callbacks interface for communication with main activity!
    public interface Callbacks {
        boolean getToggleButtonStatus();

        ControlData getControlData();

        boolean getDebugButtonStatus();

        ControlData setControlDataDebug();

        ToggleButton getToggleButton(String tagTab);

        Boolean getConnectionButtonTab2Status();

        int getSpinnerEngine();

        ControlData getDebugControlData();
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