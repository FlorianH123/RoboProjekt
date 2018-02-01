package com.mobileapplicationdev.roboproject.utils;

public class Util {
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
}
