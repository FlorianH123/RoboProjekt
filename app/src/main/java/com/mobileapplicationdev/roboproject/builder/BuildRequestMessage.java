package com.mobileapplicationdev.roboproject.builder;

import android.util.Log;

import com.mobileapplicationdev.roboproject.models.ControlData;
import com.mobileapplicationdev.roboproject.models.MessageType;
import com.mobileapplicationdev.roboproject.models.Task;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.mobileapplicationdev.roboproject.utils.Utils.swap;

/**
 * Created by Florian on 02.03.2018.
 */

public abstract class BuildRequestMessage {
    public static void sendSetTarget(DataOutputStream dataOutputStream,
                                     int engineId) throws IOException {

        int messageType = swap(MessageType.SET_TARGET.getMessageType());
        int packageSize = swap(16);
        int taskId = swap(Task.Antriebsregelung.getTaskId());
        engineId = swap(0);

        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        byteWriter.writeInt(messageType);
        Log.d("Test", "Set Target \n");
        Log.d("Test", "Message Type: " + messageType + "\n");
        byteWriter.writeInt(packageSize);
        Log.d("Test", "PackageSize: " + packageSize + "\n");
        byteWriter.writeInt(taskId);
        Log.d("Test", "TaskId: " + taskId + "\n");
        byteWriter.writeInt(engineId);
        Log.d("Test", "Engine: " + engineId + "\n");

        debugData = byteArrayStream.toByteArray();
        //byteArrayStream.reset();
        dataOutputStream.write(debugData);

        byteArrayStream.close();
        byteWriter.close();
        //dataOutputStream.close();
    }

    public static void sendGetPID(DataOutputStream dataOutputStream) throws IOException {
        int messageType = swap(MessageType.GET_PID.getMessageType());
        int packageSize = swap(8);

        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        byteWriter.writeInt(messageType);
        Log.d("Test", "Get PID \n");
        Log.d("Test", "Message Type: " + messageType + "\n");
        byteWriter.writeInt(packageSize);
        Log.d("Test", "PackageSize: " + packageSize + "\n");

        debugData = byteArrayStream.toByteArray();
        //byteArrayStream.reset();
        dataOutputStream.write(debugData);

        byteArrayStream.close();
        byteWriter.close();
        //dataOutputStream.close();
    }

    public static void sendSetPID(DataOutputStream dataOutputStream,
                                     ControlData controlData) throws IOException {

        int messageType = swap(MessageType.SET_PID.getMessageType());
        int packageSize = swap(20);
        float p = swap(controlData.getVarP());
        float i = swap(controlData.getVarI());
        float d = 0; //swap(controlData.getRegulatorFrequency());

        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        byteWriter.writeInt(messageType);
        Log.d("Test", "SET PID \n");
        Log.d("Test", "Message Type: " + messageType + "\n");
        byteWriter.writeInt(packageSize);
        Log.d("Test", "PackageSize: " + packageSize + "\n");
        byteWriter.writeFloat(p);
        Log.d("Test", "P: " + p + "\n");
        byteWriter.writeFloat(i);
        Log.d("Test", "I: " + i + "\n");
        byteWriter.writeFloat(0f);
        Log.d("Test", "D: " + 0 + "\n");

        debugData = byteArrayStream.toByteArray();
        //byteArrayStream.reset();
        dataOutputStream.write(debugData);

        byteArrayStream.close();
        byteWriter.close();
        //dataOutputStream.close();
    }

    public static void sendSetSpeed(DataOutputStream dataOutputStream,
                                    ControlData controlData) throws IOException {

        int messageType = swap(MessageType.SET_VALUE.getMessageType());
        int packageSize = swap(12);
        float speed = swap(controlData.getSpeed());

        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        byteWriter.writeInt(messageType);
        Log.d("Test", "SET Speed \n");
        Log.d("Test", "Message Type: " + messageType + "\n");
        byteWriter.writeInt(packageSize);
        Log.d("Test", "PackageSize: " + packageSize + "\n");
        byteWriter.writeFloat(speed);
        Log.d("Test", "Speed: " + speed + "\n");


        debugData = byteArrayStream.toByteArray();
        //byteArrayStream.reset();
        dataOutputStream.write(debugData);

        byteArrayStream.close();
        byteWriter.close();
        //dataOutputStream.close();
    }

    public static void sendConnect(DataOutputStream dataOutputStream,
                                         int port) throws IOException {

        int messageType = swap(MessageType.CONNECT.getMessageType());
        int packageSize = swap(12);
        port = swap(port);

        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        dataOutputStream.writeInt(messageType);
        Log.d("Test", "SET PID \n");
        Log.d("Test", "Message Type: " + messageType + "\n");
        dataOutputStream.writeInt(packageSize);
        Log.d("Test", "PackageSize: " + packageSize + "\n");
        dataOutputStream.writeInt(port);
        Log.d("Test", "Port: " + port + "\n");

        debugData = byteArrayStream.toByteArray();
        //byteArrayStream.reset();
        dataOutputStream.write(debugData);

        byteArrayStream.close();
        byteWriter.close();
        //dataOutputStream.close();
    }
}
