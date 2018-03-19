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
    private static final String CONTROL_THREAD_NAME = "robot_control.socket.thread";
    private static final String DEBUG_THREAD_NAME = "robot_debug.socket.thread";
    private static final String RECEIVE_DATA_THREAD_NAME = "robot_receive_data.server_socket.thread";

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
        }, CONTROL_THREAD_NAME).start();
    }

    public void openDebugSocket(final String ip, final int port, final Object waiter,
                                final int tabId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String tab_tag = null;

                try (Socket debugSocket = new Socket(ip, port);
                     ServerSocket serverSocket = new ServerSocket(0);
                     DataOutputStream dataOS = new DataOutputStream(debugSocket.getOutputStream());
                     DataInputStream dataIS = new DataInputStream(debugSocket.getInputStream())) {

                    if (tabId == MainActivity.TAB_ID_2) {
                        tab_tag = MainActivity.TAG_TAB_2;
                    } else if (tabId == MainActivity.TAB_ID_3) {
                        tab_tag = MainActivity.TAG_TAB_3;
                    }

                    // send target
                    sendTarget(dataIS, dataOS, tabId);

                    // get current P I D
                    requestPIDValues(dataIS, dataOS, tabId);

                    // wait until debug button is activated
                    synchronized (waiter) {
                        try {
                            waiter.wait();
                        } catch (InterruptedException ex) {
                            Log.e(className, ex.getMessage());
                        }
                    }

                    // send new P I D
                    sendPIDValues(dataIS, dataOS, tabId);

                    // send velocity or angle
                    if (tabId == MainActivity.TAB_ID_2) {
                        sendVelocity(dataIS, dataOS);
                    } else if (tabId == MainActivity.TAB_ID_3) {
                        sendAngle(dataIS, dataOS);
                    }

//                    // start new socket
//                    startServerSocket(serverSocket, tabId);

                    // send connect
                    sendConnect(dataIS, dataOS, serverSocket.getLocalPort());

                } catch (IOException ex) {
                    exceptionHandler(tab_tag, ex.getMessage());
                }

                stopSelf();
            }
        }, DEBUG_THREAD_NAME).start();
    }

    // TODO überarbeiten
    private void startServerSocket(final ServerSocket serverSocket, final int tabId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket clientSocket;
                DataInputStream dataInputStream;
                int messageType;
                int messageSize;
                float[] dataMr;

                try {
                    clientSocket = serverSocket.accept();
                    dataInputStream = new DataInputStream(clientSocket.getInputStream());

                    while (mainActivity.getDebugButtonStatus(tabId)) {
                        messageType = swap(dataInputStream.readInt());
                        messageSize = swap(dataInputStream.readInt());

                        Log.d(className, "MessageType: " + messageType);
                        Log.d(className, "MessageSize: " + messageSize);

                        messageSize = messageSize - 8;

                        if (messageType != MessageType.ERROR.getMessageType()) {
                            dataMr = new float[256];

                            for (int j = 0; j < messageSize; j++) {
                                dataMr[j] = swap(dataInputStream.readFloat());
                                Log.d(className, "Geschwindigkeit" + dataMr[j] + "\n");
                            }
                            // TODO Geschwindigkeit in den Graph übernehemen
                        }
                    }

                    clientSocket.close();
                    serverSocket.close();
                } catch (IOException ex) {
                    Log.e(className, ex.getMessage());
                }
            }
        }, RECEIVE_DATA_THREAD_NAME).start();
    }

    /**
     * Send target information to the robot
     * <p>
     * Send
     * - message type
     * - message size
     * - taskId
     * - engineId
     * <p>
     * Receive response
     * - message type
     * - message size
     *
     * @param dataInputStream  input stream from robot
     * @param dataOutputStream output stream to robot
     * @param tabId            tab id of the gui
     * @throws IOException io exception
     */
    private void sendTarget(DataInputStream dataInputStream,
                            DataOutputStream dataOutputStream,
                            int tabId) throws IOException {

        int messageType;
        int messageSize;
        int taskId;
        int engineId;
        byte[] targetData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        // Request ---------------------------------------------------------------------------------
        messageType = MessageType.SET_TARGET.getMessageType();
        messageSize = 16;
        taskId = Task.Antriebsregelung.getTaskId();
        engineId = mainActivity.getSpinnerEngine(tabId);

        Log.d(className, "Send Set Target");
        Log.d(className, "Message Type: " + messageType);
        Log.d(className, "PackageSize: " + messageSize);
        Log.d(className, "TaskId: " + taskId);
        Log.d(className, "Engine: " + engineId);

        byteWriter.writeInt(swap(messageType));
        byteWriter.writeInt(swap(messageSize));
        byteWriter.writeInt(swap(taskId));
        byteWriter.writeInt(swap(engineId));

        targetData = byteArrayStream.toByteArray();
        dataOutputStream.write(targetData);
        // -----------------------------------------------------------------------------------------

        // Response --------------------------------------------------------------------------------
        messageType = swap(dataInputStream.readInt());
        messageSize = swap(dataInputStream.readInt());

        Log.d(className, "Receive Set Target");
        Log.d(className, "MessageType: " + messageType);
        Log.d(className, "MessageSize: " + messageSize);

        if (messageType == MessageType.ERROR.getMessageType()) {
            throw new IOException(getString(R.string.error_msg_sending_target));
        }
        // -----------------------------------------------------------------------------------------

        byteArrayStream.close();
        byteWriter.close();
    }

    /**
     * Request P I D values from robot
     * <p>
     * Send
     * - message type
     * - message size
     * <p>
     * Response
     * - message type
     * - message size
     * - p value
     * - i value
     * - d value
     *
     * @param dataInputStream  input stream from robot
     * @param dataOutputStream output stream to robot
     * @param tabId            tab id of the gui
     * @throws IOException io exception
     */
    private void requestPIDValues(DataInputStream dataInputStream,
                                  DataOutputStream dataOutputStream,
                                  int tabId) throws IOException {

        int messageType;
        int messageSize;
        float p;
        float i;
        float d;
        byte[] pidData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        // Request ---------------------------------------------------------------------------------
        messageType = MessageType.GET_PID.getMessageType();
        messageSize = 8;

        Log.d(className, "Send Get Pid");
        Log.d(className, "Message Type: " + messageType);
        Log.d(className, "PackageSize: " + messageSize);

        byteWriter.writeInt(swap(messageType));
        byteWriter.writeInt(swap(messageSize));

        pidData = byteArrayStream.toByteArray();
        dataOutputStream.write(pidData);
        // -----------------------------------------------------------------------------------------

        // Response --------------------------------------------------------------------------------
        messageType = swap(dataInputStream.readInt());
        messageSize = swap(dataInputStream.readInt());

        Log.d(className, "Receive get pid");
        Log.d(className, "MessageType: " + messageType);
        Log.d(className, "MessageSize: " + messageSize);

        if (messageType == MessageType.ERROR.getMessageType()) {
            throw new IOException(getString(R.string.error_msg_requesting_pid));
        } else {

            p = swap(dataInputStream.readFloat());
            i = swap(dataInputStream.readFloat());
            // Unused
            d = swap(dataInputStream.readFloat());

            Log.d(className, "P: " + p);
            Log.d(className, "I: " + i);
            Log.d(className, "D: " + d);

            mainActivity.setP(p, tabId);
            mainActivity.setI(i, tabId);
            mainActivity.enableDebugButton(tabId);
        }
        // -----------------------------------------------------------------------------------------

        byteArrayStream.close();
        byteWriter.close();
    }

    /**
     * Send P I D values
     * <p>
     * Send
     * - message type
     * - message size
     * - p value
     * - i value
     * - d value
     * <p>
     * Response
     * - message type
     * - message size
     *
     * @param dataInputStream  input stream from robot
     * @param dataOutputStream output stream to robot
     * @param tabId            tab id of the gui
     * @throws IOException io exception
     */
    private void sendPIDValues(DataInputStream dataInputStream,
                               DataOutputStream dataOutputStream,
                               int tabId) throws IOException {
        int messageType;
        int messageSize;
        float p;
        float i;
        float d;
        byte[] pidData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        // Request ---------------------------------------------------------------------------------
        messageType = MessageType.SET_PID.getMessageType();
        messageSize = 20;

        p = mainActivity.getP(tabId);
        i = mainActivity.getI(tabId);
        d = 0f;

        Log.d(className, "Send Set PID");
        Log.d(className, "MessageType: " + messageType);
        Log.d(className, "MessageSize: " + messageSize);
        Log.d(className, "P: " + p);
        Log.d(className, "I: " + i);
        Log.d(className, "D: " + d);

        byteWriter.writeInt(swap(messageType));
        byteWriter.writeInt(swap(messageSize));
        byteWriter.writeFloat(swap(p));
        byteWriter.writeFloat(swap(i));
        byteWriter.writeFloat(swap(d));

        pidData = byteArrayStream.toByteArray();
        dataOutputStream.write(pidData);
        // -----------------------------------------------------------------------------------------

        // Response --------------------------------------------------------------------------------
        messageType = swap(dataInputStream.readInt());
        messageSize = swap(dataInputStream.readInt());

        Log.d(className, "Receive Set PID");
        Log.d(className, "MessageType: " + messageType);
        Log.d(className, "MessageSize: " + messageSize);

        if (messageType == MessageType.ERROR.getMessageType()) {
            throw new IOException(getString(R.string.error_msg_receiving_pid));
        }
        // -----------------------------------------------------------------------------------------

        byteArrayStream.close();
        byteWriter.close();
    }

    /**
     * Send velocity to robot
     * <p>
     * Send
     * - message type
     * - message size
     * - velocity
     * <p>
     * Response
     * - message type
     * - message size
     *
     * @param dataInputStream  input stream from robot
     * @param dataOutputStream output stream to robot
     * @throws IOException io exception
     */
    private void sendVelocity(DataInputStream dataInputStream,
                              DataOutputStream dataOutputStream) throws IOException {
        int messageType;
        int messageSize;
        byte[] velocityData;
        float velocity;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        // Request ---------------------------------------------------------------------------------
        messageType = MessageType.SET_VALUE.getMessageType();
        messageSize = 12;
        velocity = mainActivity.getVelocity();

        Log.d(className, "Send Set Velocity");
        Log.d(className, "MessageType: " + messageType);
        Log.d(className, "PackageSize: " + messageSize);
        Log.d(className, "Velocity: " + velocity);

        byteWriter.writeInt(swap(messageType));
        byteWriter.writeInt(swap(messageSize));
        byteWriter.writeFloat(swap(velocity));

        velocityData = byteArrayStream.toByteArray();
        dataOutputStream.write(velocityData);
        // -----------------------------------------------------------------------------------------

        // Response --------------------------------------------------------------------------------
        messageType = swap(dataInputStream.readInt());
        messageSize = swap(dataInputStream.readInt());

        Log.d(className, "Receive Set Velocity");
        Log.d(className, "MessageType: " + messageType);
        Log.d(className, "MessageSize: " + messageSize);

        if (messageType == MessageType.ERROR.getMessageType()) {
            throw new IOException(getString(R.string.error_msg_sending_velocity));
        }
        // -----------------------------------------------------------------------------------------

        byteArrayStream.close();
        byteWriter.close();
    }

    /**
     * Send angle to robot
     * <p>
     * Send
     * - message type
     * - message size
     * - angle
     * <p>
     * Response
     * - message type
     * - message size
     *
     * @param dataInputStream  input stream from robot
     * @param dataOutputStream output stream to robot
     * @throws IOException io exception
     */
    private void sendAngle(DataInputStream dataInputStream,
                           DataOutputStream dataOutputStream) throws IOException {

        int messageType;
        int messageSize;
        byte[] angleData;
        int angle;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        // Request ---------------------------------------------------------------------------------
        messageType = MessageType.SET_VALUE.getMessageType();
        messageSize = 12;
        angle = mainActivity.getAngle();

        Log.d(className, "Send Set Angle");
        Log.d(className, "MessageType: " + messageType);
        Log.d(className, "PackageSize: " + messageSize);
        Log.d(className, "Angle: " + angle);

        byteWriter.writeInt(swap(messageType));
        byteWriter.writeInt(swap(messageSize));
        byteWriter.writeInt(swap(angle));

        angleData = byteArrayStream.toByteArray();
        dataOutputStream.write(angleData);
        // -----------------------------------------------------------------------------------------

        // Response --------------------------------------------------------------------------------
        messageType = swap(dataInputStream.readInt());
        messageSize = swap(dataInputStream.readInt());

        Log.d(className, "Receive Set Angle");
        Log.d(className, "MessageType: " + messageType);
        Log.d(className, "MessageSize: " + messageSize);

        if (messageType == MessageType.ERROR.getMessageType()) {
            throw new IOException(getString(R.string.error_msg_sending_angle));
        }
        // -----------------------------------------------------------------------------------------

        byteArrayStream.close();
        byteWriter.close();
    }

    /**
     * Send connect
     * <p>
     * Send
     * - message type
     * - message size
     * - port
     * <p>
     * Response
     * - message type
     * - message size
     *
     * @param dataInputStream  input stream from robot
     * @param dataOutputStream output stream to robot
     * @param port             port of the data server socket
     * @throws IOException io exception
     */
    private void sendConnect(DataInputStream dataInputStream,
                             DataOutputStream dataOutputStream,
                             int port) throws IOException {

        int messageType;
        int messageSize;
        byte[] connectionData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        // Request ---------------------------------------------------------------------------------
        messageType = MessageType.CONNECT.getMessageType();
        messageSize = 12;

        Log.d(className, "Send Connect");
        Log.d(className, "Message Type: " + messageType);
        Log.d(className, "PackageSize: " + messageSize);
        Log.d(className, "Port: " + port);

        dataOutputStream.writeInt(swap(messageType));
        dataOutputStream.writeInt(swap(messageSize));
        dataOutputStream.writeInt(swap(port));

        connectionData = byteArrayStream.toByteArray();
        dataOutputStream.write(connectionData);
        // -----------------------------------------------------------------------------------------

        // Response --------------------------------------------------------------------------------
        messageType = swap(dataInputStream.readInt());
        messageSize = swap(dataInputStream.readInt());

        Log.d(className, "Receive Connect");
        Log.d(className, "MessageType: " + messageType);
        Log.d(className, "MessageSize: " + messageSize);

        if (messageType == MessageType.ERROR.getMessageType()) {
            throw new IOException(getString(R.string.error_msg_connecting));
        }
        // -----------------------------------------------------------------------------------------

        byteArrayStream.close();
        byteWriter.close();
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

        float getVelocity();

        int getAngle();

        void enableDebugButton(int tabId);
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