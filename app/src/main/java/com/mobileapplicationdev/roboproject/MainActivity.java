package com.mobileapplicationdev.roboproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTabHost();
        initConnectionButton();
        initDriveModeSwitch();
        initJoyStick();

        initButton();
    }

    private void initTabHost() {
        String tabNameTab1 = (String) getResources().getText(R.string.tab_name_tab1);
        String tabNameTab2 = (String) getResources().getText(R.string.tab_name_tab2);
        String tabNameTab3 = (String) getResources().getText(R.string.tab_name_tab3);

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
                Toast.makeText(MainActivity.this, tagName , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initConnectionButton() {
        final ToggleButton toggle = findViewById(R.id.toggleButton_connection);
        final EditText editText_ipAddress = findViewById(R.id.editText_ipAdress);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String ipAddress;
                String errMsgInvalidIp = getResources().getString(R.string.error_msg_invalid_ip);

                if (isChecked) {
                    ipAddress = String.valueOf(editText_ipAddress.getText());

                    if (!ipAddress.equals("")){
                        //TODO open Socket
                        Toast.makeText(MainActivity.this,"Activated" , Toast.LENGTH_SHORT).show();
                        editText_ipAddress.setEnabled(false);
                    } else {
                        toggle.setChecked(false);
                        Toast.makeText(MainActivity.this,errMsgInvalidIp , Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // TODO close Socket
                    Toast.makeText(MainActivity.this, "Deactivated" , Toast.LENGTH_SHORT).show();
                    editText_ipAddress.setEnabled(true);
                }
            }
        });
    }

    private void initDriveModeSwitch() {
        final String switchTextOn = (String) getResources().getText(R.string.text_switch_driveModus_on);
        final String switchTextOff = (String) getResources().getText(R.string.text_switch_driveModus_off);

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
            TextView textView = findViewById(R.id.textView);

            @Override
            public void onDown() {
                // ..
            }

            @Override
            public void onDrag(float degrees, float offset) {
                textView.setText(String.valueOf((int) degrees) + " " + offset);
            }

            @Override
            public void onUp() {
                textView.setText("0, 0");
            }
        });
    }

    private void initButton() {
        final Button button = findViewById(R.id.button);
        final Button buttonDown = findViewById(R.id.button2);
        button.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    button.setBackground(getResources().getDrawable(R.drawable.arrow_up));
                    // Do what you want
                    return true;
                } else {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        button.setBackground(getResources().getDrawable(R.drawable.arrow_up_pushed));
                        return true;
                    }
                }
                return false;
            }
        });

        buttonDown.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    buttonDown.setBackground(getResources().getDrawable(R.drawable.arrow_down));
                    // Do what you want
                    return true;
                } else {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        buttonDown.setBackground(getResources().getDrawable(R.drawable.arrow_down_pushed));
                        return true;
                    }
                }
                return false;
            }
        });

    }
}
