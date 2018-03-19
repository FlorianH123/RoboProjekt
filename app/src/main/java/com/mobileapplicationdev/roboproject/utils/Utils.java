package com.mobileapplicationdev.roboproject.utils;

import android.graphics.Color;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

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

    public static float getTimeX(float herz){
        return (1/herz);
    }

    public static LineDataSet createSetTwo() {
        LineDataSet set = new LineDataSet(null, "Static Data");

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#B40404"));
        set.setCircleColor(Color.RED);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(100, 170, 30));
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(9f);
        set.setDrawValues(true);

        return set;
    }

    public static LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Dynamic Data");

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setDrawFilled(true);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLUE);
        set.setValueTextSize(9f);
        set.setDrawValues(true);

        return set;
    }

    public static boolean validateInput(String iValue, String pValue, String frequency,
                                        String velocity) {
        return iValue.trim().isEmpty() || pValue.trim().isEmpty() || frequency.trim().isEmpty() ||
                frequency.trim().isEmpty() || velocity.trim().isEmpty();

    }
}
