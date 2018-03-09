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
import com.mobileapplicationdev.roboproject.models.Task;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

    public void openDebugSocket(final String ip, final int port) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int tabId = 2;

                try (Socket debugSocket = new Socket(ip, port);
                     ServerSocket serverSocket = new ServerSocket(0);
                     DataOutputStream dataOS = new DataOutputStream(debugSocket.getOutputStream());
                     DataInputStream dataIS = new DataInputStream(debugSocket.getInputStream())) {

                    // Send target
                    sendTarget(dataIS, dataOS, tabId);

//                    // Get current PID
//                    requestPIDValues(dataIS, dataOS);
//
//                    // Send new PID
//                    sendPIDValues(dataIS, dataOS, tabId);
//
//                    // Send velocity
//                    sendVelocity(dataIS, dataOS);
//
//                    // TODO start new socket
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            final String logName = "receivingData";
//                            Socket clientSocket;
//                            DataInputStream dataInputStream;
//                            int messageType;
//                            int messageSize;
//                            float[] dataMr;
//
//                            try {
//                                clientSocket = serverSocket.accept();
//                                dataInputStream = new DataInputStream(clientSocket.getInputStream());
//
//                                while (mainActivity.getDebugButtonStatus(tabId)) {
//                                    messageType = swap(dataInputStream.readInt());
//                                    messageSize = swap(dataInputStream.readInt());
//
//                                    Log.d(logName, "MessageType: " + messageType);
//                                    Log.d(logName, "MessageSize: " + messageSize);
//
//                                    messageSize = messageSize - 8;
//
//                                    if (messageType != MessageType.ERROR.getMessageType()) {
//                                        dataMr = new float[256];
//
//                                        for (int j = 0; j < messageSize; j++) {
//                                            dataMr[j] = swap(dataInputStream.readFloat());
//                                            Log.d(logName, "Geschwindigkeit" + dataMr[j] + "\n");
//                                        }
//                                        // TODO Geschwindigkeit in den Graph übernehemen
//                                    }
//                                }
//                            } catch (IOException ex) {
//                                ex.printStackTrace();
//                            }
//                        }
//                    }, "robot_receive.socket.thread").start();
                } catch (IOException ex) {
                    exceptionHandler(MainActivity.TAG_TAB_2, ex.getMessage());
                }

                stopSelf();
            }
        }, "robot_debug.socket.thread").start();
    }

    private void sendTarget(DataInputStream dataInputStream, DataOutputStream dataOutputStream, int tabId) throws IOException {
        final String logName = "sendTarget";
        int messageType;
        int messageSize;
        int taskId;
        int engineId;
        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        messageType = swap(MessageType.SET_TARGET.getMessageType());
        messageSize = swap(16);
        taskId = swap(Task.Antriebsregelung.getTaskId());
        engineId = swap(mainActivity.getSpinnerEngine(tabId));

        byteWriter.writeInt(messageType);
        byteWriter.writeInt(messageSize);
        byteWriter.writeInt(taskId);
        byteWriter.writeInt(engineId);

        Log.d(logName, "Set Target");
        Log.d(logName, "Message Type: " + messageType);
        Log.d(logName, "PackageSize: " + messageSize);
        Log.d(logName, "TaskId: " + taskId);
        Log.d(logName, "Engine: " + engineId);

        debugData = byteArrayStream.toByteArray();
        dataOutputStream.write(debugData);

        messageType = swap(dataInputStream.readInt());
        messageSize = swap(dataInputStream.readInt());

        Log.d(logName, "Read Set Target");
        Log.d(logName, "MessageType: " + messageType + "\n");
        Log.d(logName, "MessageSize: " + messageSize + "\n");

        if (messageType == MessageType.ERROR.getMessageType()) {
            throw new IOException("Error while sending target");
        }

        byteArrayStream.close();
        byteWriter.close();
        dataInputStream.close();
        dataOutputStream.close();
    }

    private void requestPIDValues(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        final String logName = "requestPIDValues";
        int messageType;
        int messageSize;
        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        messageType = swap(MessageType.GET_PID.getMessageType());
        messageSize = swap(8);

        byteWriter.writeInt(messageType);
        byteWriter.writeInt(messageSize);

        Log.d(logName, "Send get pid");
        Log.d(logName, "Message Type: " + messageType);
        Log.d(logName, "PackageSize: " + messageSize);

        debugData = byteArrayStream.toByteArray();
        dataOutputStream.write(debugData);

        messageType = swap(dataInputStream.readInt());
        messageSize = swap(dataInputStream.readInt());

        Log.d(logName, "Read get pid");
        Log.d(logName, "MessageType: " + messageType);
        Log.d(logName, "MessageSize: " + messageSize);

        if (messageType == MessageType.ERROR.getMessageType()) {
            throw new IOException("Error while requesting pid values");
        } else {
            //mainActivity.setP(dataIS.readFloat(), tabId);
            Log.d(logName, "Read P" + swap(dataInputStream.readFloat()));
            //mainActivity.setI(dataIS.readFloat(), tabId);
            Log.d(logName, "Read I" + swap(dataInputStream.readFloat()));
            //mainActivity.setD(dataIS.readFloat(), tabId);
            Log.d(logName, "Read D" + swap(dataInputStream.readFloat()));
            //TODO Werte in GUI eintragen
        }

        byteArrayStream.close();
        byteWriter.close();
        dataInputStream.close();
        dataOutputStream.close();
    }

    private void sendPIDValues(DataInputStream dataInputStream, DataOutputStream dataOutputStream, int tabId) throws IOException {
        final String logName = "sendPIDValues";
        int messageType;
        int messageSize;
        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        messageType = swap(MessageType.SET_PID.getMessageType());
        messageSize = swap(20);

        float p = swap(mainActivity.getP(tabId));
        float i = swap(mainActivity.getI(tabId));
        float d = 0f;

        byteWriter.writeInt(messageType);
        byteWriter.writeInt(messageSize);
        byteWriter.writeFloat(p);
        byteWriter.writeFloat(i);
        byteWriter.writeFloat(d);

        Log.d(logName, "SET PID");
        Log.d(logName, "Message Type: " + messageType);
        Log.d(logName, "PackageSize: " + messageSize);
        Log.d(logName, "P: " + swap(p));
        Log.d(logName, "I: " + swap(i));
        Log.d(logName, "D: " + 0);

        debugData = byteArrayStream.toByteArray();
        dataOutputStream.write(debugData);

        messageType = swap(dataInputStream.readInt());
        messageSize = swap(dataInputStream.readInt());

        Log.d(logName, "Read Set PID");
        Log.d(logName, "MessageType: " + messageType);
        Log.d(logName, "MessageSize: " + messageSize);

        if (messageType == MessageType.ERROR.getMessageType()) {
            throw new IOException("Error while sending pid values");
        }

        byteArrayStream.close();
        byteWriter.close();
        dataInputStream.close();
        dataOutputStream.close();
    }

    private void sendVelocity(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        final String logName = "sendVelocity";
        int messageType;
        int messageSize;
        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        messageType = swap(MessageType.SET_VALUE.getMessageType());
        messageSize = swap(12);
        float speed = swap(mainActivity.getSpeed());

        byteWriter.writeInt(messageType);
        byteWriter.writeInt(messageSize);
        byteWriter.writeFloat(speed);

        Log.d(logName, "SET Speed");
        Log.d(logName, "Message Type: " + messageType);
        Log.d(logName, "PackageSize: " + messageSize);
        Log.d(logName, "Speed: " + speed);


        debugData = byteArrayStream.toByteArray();
        byteArrayStream.reset();
        dataOutputStream.write(debugData);

        messageType = swap(dataInputStream.readInt());
        messageSize = swap(dataInputStream.readInt());

        Log.d(logName, "Read Set Speed");
        Log.d(logName, "MessageType: " + messageType);
        Log.d(logName, "MessageSize: " + messageSize);

        if (messageType == MessageType.ERROR.getMessageType()) {
            throw new IOException("Error while sending velocity");
        }
    }

    private void sendConnect(DataInputStream dataInputStream, DataOutputStream dataOutputStream, int port) throws IOException {
        final String logName = "sendConnect";
        int messageType = swap(MessageType.CONNECT.getMessageType());
        int messageSize = swap(12);
        port = swap(port);
        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        dataOutputStream.writeInt(messageType);
        dataOutputStream.writeInt(messageSize);
        dataOutputStream.writeInt(port);

        Log.d(logName, "SET PID");
        Log.d(logName, "Message Type: " + messageType);
        Log.d(logName, "PackageSize: "  + messageSize);
        Log.d(logName, "Port: "         + port);

        debugData = byteArrayStream.toByteArray();
        dataOutputStream.write(debugData);

        messageType = swap(dataInputStream.readInt());
        messageSize = swap(dataInputStream.readInt());

        Log.d(logName, "Receive Connect");
        Log.d(logName, "MessageType: " + messageType);
        Log.d(logName, "MessageSize: " + messageSize);

        if (messageType == MessageType.ERROR.getMessageType()) {
            throw new IOException("Error while connecting");
        }

        byteArrayStream.close();
        byteWriter.close();
        dataOutputStream.close();
        dataInputStream.close();
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

        float getP(int tabId);

        void setI(float i, int tabId);

        float getI(int tabId);

        void setD(float d, int tabId);

        float getD(int tabId);

        float getSpeed();
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