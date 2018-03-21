package com.mobileapplicationdev.roboproject.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
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
    private final String className = SocketService.class.getSimpleName();

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
                    Log.e(className, ex.getMessage());
                    exceptionHandler(MainActivity.TAG_TAB_1, MainActivity.TAB_ID_1, ex.getMessage());
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
                String tabTag = null;
                AddEntryGraphThread addEntryGraphThread = null;

                if (tabId == MainActivity.TAB_ID_2) {
                    tabTag = MainActivity.TAG_TAB_2;
                } else if (tabId == MainActivity.TAB_ID_3) {
                    tabTag = MainActivity.TAG_TAB_3;
                }

                try (Socket debugSocket = new Socket(ip, port);
                     DataOutputStream dataOS = new DataOutputStream(debugSocket.getOutputStream());
                     DataInputStream dataIS = new DataInputStream(debugSocket.getInputStream())) {

                    ServerSocket serverSocket = new ServerSocket(0);

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
                        addEntryGraphThread = new AddEntryGraphThread(
                                mainActivity.getLineChart(tabId), mainActivity.getVelocity());
                    } else if (tabId == MainActivity.TAB_ID_3) {
                        sendAngle(dataIS, dataOS);
                        addEntryGraphThread = new AddEntryGraphThread(
                                mainActivity.getLineChart(tabId), mainActivity.getAngle());
                    }

                    // start new socket
                    if (addEntryGraphThread != null) {
                        addEntryGraphThread.startThread();
                    }
                    startServerSocket(serverSocket, tabId, tabTag, addEntryGraphThread);

                    // send connect
                    sendConnect(dataIS, dataOS, serverSocket.getLocalPort());

                } catch (IOException ex) {
                    Log.e(className, ex.getMessage());
                    exceptionHandler(tabTag, tabId, ex.getMessage());
                }
            }
        }, DEBUG_THREAD_NAME).start();
    }

    private void startServerSocket(final ServerSocket serverSocket,
                                   final int tabId,
                                   final String tabTag,
                                   final AddEntryGraphThread addEntryGraphThread) {

        new Thread(new Runnable() {
            @Override
            public void run() {


                int messageType;
                int messageSize;

                try (Socket clientSocket = serverSocket.accept();
                     DataInputStream dataInputStream =
                             new DataInputStream(clientSocket.getInputStream())){

                    while (mainActivity.getDebugButtonStatus(tabId)) {
                        messageType = swap(dataInputStream.readInt());
                        messageSize = swap(dataInputStream.readInt());

                        Log.d(className, "ReceiveData: ");
                        Log.d(className, "MessageType: " + messageType);
                        Log.d(className, "MessageSize: " + messageSize);

                        messageSize = messageSize / 4 - 2;

                        if (messageType != MessageType.ERROR.getMessageType()) {
                            if (tabId == MainActivity.TAB_ID_2) {
                                receiveVelocity(dataInputStream, messageSize, addEntryGraphThread);
                            } else if (tabId == MainActivity.TAB_ID_3) {
                                receiveAngle(dataInputStream, messageSize, addEntryGraphThread);
                            }
                        } else {
                            throw new IOException(getString(R.string.error_msg_receiving_data));
                        }
                    }

                    serverSocket.close();
                    addEntryGraphThread.stop();
                } catch (IOException ex) {
                    Log.e(className, ex.getMessage());
                    exceptionHandler(tabTag, tabId, ex.getMessage());
                } finally {
                    try {
                        if (serverSocket != null) {
                            serverSocket.close();
                        }
                    } catch (IOException ex) {
                        Log.e(className, ex.getMessage());
                        exceptionHandler(tabTag, tabId, ex.getMessage());
                    }
                }
            }
        }, RECEIVE_DATA_THREAD_NAME).start();
    }

    private void receiveVelocity(DataInputStream dataInputStream,
                                 int messageSize,
                                 AddEntryGraphThread addEntryGraphThread) throws IOException {

        float velocityValue;

        for (int i = 0; i < messageSize; i++) {
            velocityValue = swap(dataInputStream.readFloat());
            addEntryGraphThread.addEntry(velocityValue);
            Log.d(className, "Velocity " + velocityValue);
        }
    }

    private void receiveAngle(DataInputStream dataInputStream,
                              int messageSize,
                              AddEntryGraphThread addEntryGraphThread) throws IOException {
        int angleValue;

        for (int i = 0; i < messageSize; i++) {
            angleValue = swap(dataInputStream.readInt());
            addEntryGraphThread.addEntry(angleValue);
            Log.d(className, "Angle" + angleValue);
        }
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
        taskId = 0;

        if (tabId == MainActivity.TAB_ID_2) {
            taskId = Task.Antriebsregelung.getTaskId();
        } else if (tabId == MainActivity.TAB_ID_3) {
            taskId = Task.StellmotorPositionsregelung.getTaskId();
        }

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

        ToggleButton getDebugButton(String tagTab);

        int getSpinnerEngine(int tabId);

        void setP(float p, int tabId);

        float getP(int tabId);

        void setI(float i, int tabId);

        float getI(int tabId);

        float getD(int tabId);

        float getVelocity();

        int getAngle();

        void enableDebugButton(int tabId);

        LineChart getLineChart(int tabId);
    }

    private String getErrorMessage(String exceptionMessage) {
        String ioExceptionLoggerMsg =
                getString(R.string.error_msg_socket_io_exception);

        return ioExceptionLoggerMsg + " " + exceptionMessage;
    }

    private void exceptionHandler(String tagTab, int tabId, String ex) {
        final String errorString = getErrorMessage(ex);
        final ToggleButton connectionButton = mainActivity.getToggleButton(tagTab);

        if (tagTab.equals(MainActivity.TAG_TAB_1)) {
            connectionButton.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SocketService.this, errorString,
                            Toast.LENGTH_LONG).show();
                    connectionButton.setChecked(false);
                }
            });
        } else if (tagTab.equals(MainActivity.TAG_TAB_2) || tagTab.equals(MainActivity.TAG_TAB_3)) {
            final ToggleButton debugButton = mainActivity.getDebugButton(tagTab);

            if (mainActivity.getDebugButtonStatus(tabId)) {
                debugButton.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SocketService.this, errorString,
                                Toast.LENGTH_LONG).show();
                        debugButton.setChecked(false);
                    }
                });
            } else {
                connectionButton.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SocketService.this, errorString,
                                Toast.LENGTH_LONG).show();
                        connectionButton.setChecked(false);
                    }
                });
            }

        }
    }
}