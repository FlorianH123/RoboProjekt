package com.mobileapplicationdev.roboproject.builder;

import com.mobileapplicationdev.roboproject.models.ControlData;
import com.mobileapplicationdev.roboproject.models.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.mobileapplicationdev.roboproject.utils.Utils.swap;

/**
 * Created by Florian on 02.03.2018.
 */

public abstract class BuildRequestMessage {
    public static void sendSetTarget(DataOutputStream dataOutputStream,
                                           ControlData controlData) throws IOException {

        int messageType = swap(MessageType.SET_TARGET.getMessageType());
        int packageSize = swap(4);
        int taskId = swap(controlData.getTask().getTaskId());
        int engineId = swap(controlData.getEngine().getEngineId());

        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        byteWriter.writeInt(messageType);
        byteWriter.writeInt(packageSize);
        byteWriter.writeInt(taskId);
        byteWriter.writeInt(engineId);

        debugData = byteArrayStream.toByteArray();
        byteArrayStream.reset();
        dataOutputStream.write(debugData);

        byteArrayStream.close();
        byteWriter.close();
        dataOutputStream.close();
    }

    public static void sendGetPID(DataOutputStream dataOutputStream) throws IOException {
        int messageType = swap(MessageType.GET_PID.getMessageType());
        int packageSize = swap(2);

        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        byteWriter.writeInt(messageType);
        byteWriter.writeInt(packageSize);

        debugData = byteArrayStream.toByteArray();
        byteArrayStream.reset();
        dataOutputStream.write(debugData);

        byteArrayStream.close();
        byteWriter.close();
        dataOutputStream.close();
    }

    public static void sendSetPID(DataOutputStream dataOutputStream,
                                     ControlData controlData) throws IOException {

        int messageType = swap(MessageType.SET_PID.getMessageType());
        int packageSize = swap(5);
        float p = swap(controlData.getVarP());
        float i = swap(controlData.getVarI());
        float d = swap(controlData.getRegulatorFrequency());

        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        byteWriter.writeInt(messageType);
        byteWriter.writeInt(packageSize);
        byteWriter.writeFloat(p);
        byteWriter.writeFloat(i);
        byteWriter.writeFloat(d);

        debugData = byteArrayStream.toByteArray();
        byteArrayStream.reset();
        dataOutputStream.write(debugData);

        byteArrayStream.close();
        byteWriter.close();
        dataOutputStream.close();
    }

    public static void sendConnect(DataOutputStream dataOutputStream,
                                         int port) throws IOException {

        int messageType = swap(MessageType.SET_PID.getMessageType());
        int packageSize = swap(3);
        port = swap(port);

        byte[] debugData;

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream byteWriter = new DataOutputStream(byteArrayStream);

        dataOutputStream.writeInt(messageType);
        dataOutputStream.writeInt(packageSize);
        dataOutputStream.writeInt(port);

        debugData = byteArrayStream.toByteArray();
        byteArrayStream.reset();
        dataOutputStream.write(debugData);

        byteArrayStream.close();
        byteWriter.close();
        dataOutputStream.close();
    }
}
