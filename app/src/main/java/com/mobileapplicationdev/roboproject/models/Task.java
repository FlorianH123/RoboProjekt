package com.mobileapplicationdev.roboproject.models;

/**
 * Created by Frenchtoast on 02.03.2018.
 * Task Enum
 */

@SuppressWarnings("unused")
public enum Task {
    Antriebsregelung(1),
    StellmotorPositionsregelung(2),
    StellmotorRPMRegelung(3);

    private final int task;

    Task(int task) {
        this.task = task;
    }

    public int getTaskId() {
        return task;
    }
}
