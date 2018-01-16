package com.mobileapplicationdev.roboproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

public class MainActivity extends AppCompatActivity {
    public static final String TAG_TAB_1 = "Tag_Tab1";
    public static final String TAG_TAB_2 = "Tag_Tab2";
    public static final String TAG_TAB_3 = "Tag_Tab3";

    private static final int CHANGE_ANGLE = 5;
    private static final int MAX_ANGLE = 360;
    private static final int MIN_ANGLE = 0;

    private static final int MAX_SPEED = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTabHost();
        initConnectionButton();
        initDriveModeSwitch();
        initJoyStick();
        initSpeedSeekBar();
    }

    private void initTabHost() {
        String tabNameTab1 = getString(R.string.tab_name_tab1);
        String tabNameTab2 = getString(R.string.tab_name_tab2);
        String tabNameTab3 = getString(R.string.tab_name_tab3);

        TabHost th = findViewById(R.id.tabHost);
        th.setup();

        TabHost.TabSpec specs = th.newTabSpec(TAG_TAB_1);
        specs.setContent(R.id.tab1);
        specs.setIndicator(tabNameTab1);
        th.addTab(specs);

        specs = th.newTabSpec(TAG_TAB_2);
        specs.setContent(R.id.tab2);
        specs.setIndicator(tabNameTab2);
        th.addTab(specs);

        specs = th.newTabSpec(TAG_TAB_3);
        specs.setContent(R.id.tab3);
        specs.setIndicator(tabNameTab3);
        th.addTab(specs);

        th.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tagName) {
                //TODO close Socket
                Toast.makeText(MainActivity.this, tagName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initConnectionButton() {
        final ToggleButton toggle = findViewById(R.id.toggleButton_connection);
        final EditText editText_ipAddress = findViewById(R.id.editText_ipAdress);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String ipAddress;
                String errMsgInvalidIp = getString(R.string.error_msg_invalid_ip);

                if (isChecked) {
                    ipAddress = String.valueOf(editText_ipAddress.getText());

                    if (!ipAddress.equals("")) {
                        //TODO open Socket
                        Toast.makeText(MainActivity.this, "Activated", Toast.LENGTH_SHORT).show();
                        editText_ipAddress.setEnabled(false);
                    } else {
                        toggle.setChecked(false);
                        Toast.makeText(MainActivity.this, errMsgInvalidIp, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // TODO close Socket
                    Toast.makeText(MainActivity.this, "Deactivated", Toast.LENGTH_SHORT).show();
                    editText_ipAddress.setEnabled(true);
                }
            }
        });
    }

    private void initDriveModeSwitch() {
        final String switchTextOn = getString(R.string.text_switch_driveModus_on);
        final String switchTextOff = getString(R.string.text_switch_driveModus_off);

        final Switch driveModeSwitch = findViewById(R.id.switch_driveModus);

        driveModeSwitch.setText(switchTextOff);

        driveModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    driveModeSwitch.setText(switchTextOn);
                } else {
                    driveModeSwitch.setText(switchTextOff);
                }
            }
        });
    }

    private void initJoyStick() {
        Joystick joystick = findViewById(R.id.joystick);

        joystick.setJoystickListener(new JoystickListener() {
            final TextView textView_angle = findViewById(R.id.textView_angle);

            @Override
            public void onDown() {
                // ..
            }

            @Override
            public void onDrag(float degrees, float offset) {
                setAngle(String.valueOf((int)degrees), textView_angle);
            }

            @Override
            public void onUp() {
                setAngle("0", textView_angle);
            }
        });
    }

    private void initSpeedSeekBar() {
        SeekBar seekBar = findViewById(R.id.seekBar_speed);
        final TextView textView_speed = findViewById(R.id.textView_speed);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double speed = (progress * MAX_SPEED) / 100;
                setSpeed(String.valueOf(speed), getString(R.string.speed_unit), textView_speed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void button_increase_angle_onClick(View view) {
        TextView textView_angle = findViewById(R.id.textView_angle);

        int angle = getAngle(textView_angle) + CHANGE_ANGLE;

        if (angle > MAX_ANGLE) {
            angle = MAX_ANGLE;
        }

        setAngle(String.valueOf(angle), textView_angle);
    }

    public void button_decrease_angle_onClick(View view) {
        TextView textView_angle = findViewById(R.id.textView_angle);

        int angle = getAngle(textView_angle) - CHANGE_ANGLE;
        if (angle < MIN_ANGLE) {
            angle = MIN_ANGLE;
        }

        setAngle(String.valueOf(angle), textView_angle);
    }

    private void setSpeed(String speed, String unit, TextView textView) {
        String speedText = "%s %s";
        textView.setText(String.format(speedText, speed, unit));
    }

    private int getAngle(TextView textView) {
        String angleString = textView.getText().toString();
        return Integer.parseInt(angleString.substring(0, angleString.length() - 1));
    }

    private void setAngle(String angle, TextView textView) {
       String angleText = getString(R.string.angle_text);
       textView.setText(String.format(angleText, angle));
    }
}
