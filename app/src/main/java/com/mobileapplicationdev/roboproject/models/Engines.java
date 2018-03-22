package com.mobileapplicationdev.roboproject.models;

/**
 * Created by Frenchtoast on 02.03.2018.
 * Engine Enum
 */

@SuppressWarnings("unused")
public enum Engines {
    ENGINE_1(0),
    ENGINE_2(1),
    ENGINE_3(2);

    private final int engine;

    Engines(int engine) {
        this.engine = engine;
    }

    public int getEngineId() {
        return engine;
    }
}
