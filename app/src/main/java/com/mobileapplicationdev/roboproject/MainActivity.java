package com.mobileapplicationdev.roboproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
    private static final String TAG_TAB_1 = "Tag_Tab1";
    private static final String TAG_TAB_2 = "Tag_Tab2";
    private static final String TAG_TAB_3 = "Tag_Tab3";

    private static final int CHANGE_ANGLE = 5;
    private static final int MAX_ANGLE = 360;
    private static final int MIN_ANGLE = 0;

    private static final int MAX_SPEED = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initTabHost();
        initConnectionButton();
        initDriveModeSwitch();
        initJoyStick();
        initSpeedSeekBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settingsMenu) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

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
                // TODO close Socket
                // TODO Toast löschen
                Toast.makeText(MainActivity.this, tagName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initConnectionButton() {
        final ToggleButton toggle = findViewById(R.id.toggleButton_connection);
        final EditText editText_ipAddress = findViewById(R.id.editText_ipAddress);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String ipAddress;
                String errMsgInvalidIp = getString(R.string.error_msg_invalid_ip);

                if (isChecked) {
                    ipAddress = String.valueOf(editText_ipAddress.getText());

                    if (!ipAddress.equals("")) {
                        // TODO open Socket
                        // TODO Toast löschen
                        Toast.makeText(MainActivity.this, "Activated", Toast.LENGTH_SHORT).show();
                        editText_ipAddress.setEnabled(false);
                    } else {
                        toggle.setChecked(false);
                        Toast.makeText(MainActivity.this, errMsgInvalidIp, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // TODO close Socket
                    // TODO Toast Löschen
                    Toast.makeText(MainActivity.this, "Deactivated", Toast.LENGTH_SHORT).show();
                    editText_ipAddress.setEnabled(true);
                }
            }
        });
    }

    private void initDriveModeSwitch() {
        final String switchTextOn = getString(R.string.text_switch_driveModus_on);
        final String switchTextOff = getString(R.string.text_switch_driveModus_off);

        final Switch driveModeSwitch = findViewById(R.id.switch_driveMode);

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
            }

            @Override
            public void onDrag(float degrees, float offset) {
                setTextViewText(String.valueOf((int)degrees),
                        getString(R.string.degree), textView_angle);
            }

            @Override
            public void onUp() {
                setTextViewText("0", getString(R.string.degree), textView_angle);
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
                setTextViewText(String.valueOf(speed), getString(R.string.speed_unit), textView_speed);
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

        setTextViewText(String.valueOf(angle), getString(R.string.degree), textView_angle);
    }

    public void button_decrease_angle_onClick(View view) {
        TextView textView_angle = findViewById(R.id.textView_angle);

        int angle = getAngle(textView_angle) - CHANGE_ANGLE;
        if (angle < MIN_ANGLE) {
            angle = MIN_ANGLE;
        }

        setTextViewText(String.valueOf(angle), getString(R.string.degree), textView_angle);
    }

    private int getAngle(TextView textView) {
        String angleString = textView.getText().toString();
        return Integer.parseInt(angleString.substring(0, angleString.length() - 2));
    }

    private void setTextViewText(String value, String unit, TextView textView) {
       String template = getString(R.string.textViewTemplate);
       textView.setText(String.format(template, value, unit));
    }
}
