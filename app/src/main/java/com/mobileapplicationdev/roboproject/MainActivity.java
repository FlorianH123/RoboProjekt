package com.mobileapplicationdev.roboproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity implements SocketService.Callbacks {
    private static final String TAG_TAB_1 = "Tag_Tab1";
    private static final String TAG_TAB_2 = "Tag_Tab2";
    private static final String TAG_TAB_3 = "Tag_Tab3";

    private static final int CHANGE_ANGLE = 5;
    private static final int MAX_ANGLE = 360;
    private static final int MIN_ANGLE = 0;

    private static final int MAX_SPEED = 50;

    private SocketService socketService;
    private boolean mBound = false;
    private int drivingMode = 0;

    private boolean isForwardButtonPressed = false;
    private boolean isBackwardButtonPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, SocketService.class);
        startService(intent);
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initTabHost();
        initConnectionButton();
        initDriveModeSwitch();
        initJoyStick();
        initSpeedSeekBar();
        //initForwardAndBackwardButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            socketService = binder.getService();
            socketService.registerClient(MainActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

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
                        editText_ipAddress.setEnabled(false);
                        startSocketService();
                        // TODO Toast löschen
                        Toast.makeText(MainActivity.this, "Activated", Toast.LENGTH_SHORT).show();

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

    private void startSocketService() {
        TextView textView = findViewById(R.id.editText_ipAddress);
        String ipAddress = String.valueOf(textView.getText());

        if (mBound) {
            socketService.openSocket(ipAddress, getPreferenceKeys(0));
        }
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
                    drivingMode = 1;
                    driveModeSwitch.setText(switchTextOn);
                } else {
                    drivingMode = 0;
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

//    private void initForwardAndBackwardButton() {
//        Button forwardButton = findViewById(R.id.button_forward);
//        Button backwardButton = findViewById(R.id.button_backward);
//
//        forwardButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN) {
//                    isForwardButtonPressed = true;
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    isForwardButtonPressed = false;
//                }
//
//                return true;
//            }
//
//        });
//
//        backwardButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN) {
//                    isBackwardButtonPressed = true;
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    isBackwardButtonPressed = false;
//                }
//
//                return true;
//            }
//        });
//    }

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

    private int getSpeed() {
        TextView textView = findViewById(R.id.textView_speed);
        String speed = (String) textView.getText();
        int unitLength = getString(R.string.speed_unit).length();
        speed = speed.substring(0, speed.length() - unitLength - 1);

        return (int) Double.parseDouble(speed);
    }

    private int getPreferenceKeys(int preference) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String port1Key = getString(R.string.settings_port_1_key);
        String defaultValue1 = getString(R.string.default_value_port_1);
        String preferenceString1 = sharedPreferences.getString(port1Key, defaultValue1);

        String port2Key = getString(R.string.settings_port_1_key);
        String defaultValue2 = getString(R.string.default_value_port_1);
        String preferenceString2 = sharedPreferences.getString(port2Key, defaultValue2);

        String port3Key = getString(R.string.settings_port_1_key);
        String defaultValue3 = getString(R.string.default_value_port_1);
        String preferenceString3 = sharedPreferences.getString(port3Key, defaultValue3);

        String preferenceStringArray[] = {preferenceString1, preferenceString2, preferenceString3};

        return Integer.parseInt(preferenceStringArray[preference]);
    }

    @Override
    public boolean getToggleButtonStatus() {
        ToggleButton toggleButton = findViewById(R.id.toggleButton_connection);
        return toggleButton.isChecked();
    }

    @Override
    public boolean getForwardButtonStatus() {
        Button forwardButton = findViewById(R.id.button_forward);
        return forwardButton.isActivated();
    }

    @Override
    public boolean getBackwardButtonStatus() {
        return false;
    }

    @Override
    public ControlData getControlData() {
        TextView textView = findViewById(R.id.textView_angle);
        ControlData controlData = new ControlData();

        controlData.setDrivingMode(drivingMode);
        controlData.setAngle(getAngle(textView));
        controlData.setSpeed(getSpeed());

        return controlData;
    }
}
