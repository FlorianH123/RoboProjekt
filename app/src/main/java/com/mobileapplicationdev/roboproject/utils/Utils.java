package com.mobileapplicationdev.roboproject.utils;

public class Utils {
    /**
     * Returns if the joystick is in the first quarter of the square
     * @param angle angle
     * @return true if in first quarter
     */
    public static boolean isInFirstQuarter(float angle) {
        return angle <= 90.0 && angle >= 0.0;
    }

    /**
     * Returns if the joystick is in the second quarter of the square
     * @param angle angle
     * @return true if in second quarter
     */
    public static boolean isInSecondQuarter(float angle) {
        return angle < 0.0 && angle >= -90.0;
    }

    /**
     * Returns if the joystick is in the third quarter of the square
     * @param angle angle
     * @return true if in third quarter
     */
    public static boolean isInThirdQuarter(float angle) {
        return angle < -90.0 && angle >= -180.0;
    }

    /**
     * Returns if the joystick is in the fourth quarter of the square
     * @param angle angle
     * @return true if in fourth quarter
     */
    public static boolean isInFourthQuarter(float angle) {
        return angle > 90.0;
    }

    /**
     * Byte swap a single int value.
     *
     * @param value Value to byte swap.
     * @return Byte swapped representation.
     */
    public static int swap(int value) {
        int b1 = (value) & 0xff;
        int b2 = (value >> 8) & 0xff;
        int b3 = (value >> 16) & 0xff;
        int b4 = (value >> 24) & 0xff;

        return b1 << 24 | b2 << 16 | b3 << 8 | b4;
    }

    /**
     * Byte swap a single float value.
     *
     * @param value Value to byte swap.
     * @return Byte swapped representation.
     */
    public static float swap(float value) {
        int intValue = Float.floatToIntBits(value);
        intValue = swap(intValue);
        return Float.intBitsToFloat(intValue);
    }
}
