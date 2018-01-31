package com.mobileapplicationdev.roboproject.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;
import com.mobileapplicationdev.roboproject.R;
import com.mobileapplicationdev.roboproject.services.SocketService;
import com.mobileapplicationdev.roboproject.models.ControlData;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SocketService.Callbacks {
    private static final String TAG_TAB_1 = "Tag_Tab1";
    private static final String TAG_TAB_2 = "Tag_Tab2";
    private static final String TAG_TAB_3 = "Tag_Tab3";

    private SocketService socketService;
    private boolean mBound = false;

    private float x;
    private float y;

    private float rot_z = 0.0f;

    private final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start socket service
        Intent intent = new Intent(this, SocketService.class);
        startService(intent);
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);

        // Set toolbar icon for settings
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialise components inside  the main activity
        initTabHost();
        initConnectionButton();
        initLeftJoyStick();
        initRightJoyStick();
        initDynamicGraph();
        initSpinner();
    }

    /**
     * Returns the value of a shared preference
     * @param preferenceId number of the shared preference
     * @return the value of the preference
     */
    private String getPreferenceValue(int preferenceId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        Resources res = getResources();
        String[] portKeys = res.getStringArray(R.array.settings_port_key);
        String[] defaultValues = res.getStringArray(R.array.default_port_value);

        String portKey = portKeys[preferenceId];
        String defaultValue = defaultValues[preferenceId];

        return sp.getString(portKey, defaultValue);
    }

    private void unCheckAllConnectionButtons() {
        ToggleButton connectionButtonTab1 = findViewById(R.id.toggleButton_connection);
        //ToggleButton connectionButtonTab2 = findViewById(R.id.toggleButton_connection);
        //ToggleButton connectionButtonTab3 = findViewById(R.id.toggleButton_connection);

        connectionButtonTab1.setChecked(false);
        //connectionButtonTab2.setChecked(false);
        //connectionButtonTab3.setChecked(false);
    }

// Options Menu ------------------------------------------------------------------------------------

    /**
     * Creates the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    /**
     * Options Listener
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settingsMenu) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

// -------------------------------------------------------------------------------------------------

// TAB 1 -------------------------------------------------------------------------------------------

    private final ServiceConnection mConn = new ServiceConnection() {
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

    /**
     * Initialise tabHost with different tabs
     */
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
                unCheckAllConnectionButtons();
            }
        });
    }

    /**
     * Initialise connection button
     */
    private void initConnectionButton() {
        final ToggleButton toggle = findViewById(R.id.toggleButton_connection);
        final EditText editText_ipAddress = findViewById(R.id.editText_ipAddress);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String ipAddress;
                String errMsgInvalidIp = getString(R.string.error_msg_invalid_ip);

                if (isChecked) {
                    ipAddress = String.valueOf(editText_ipAddress.getText());

                    if (!ipAddress.trim().equals("")) {
                        editText_ipAddress.setEnabled(false);
                        startSocketService();

                    } else {
                        toggle.setChecked(false);
                        Toast.makeText(MainActivity.this,
                                errMsgInvalidIp, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    editText_ipAddress.setEnabled(true);
                }
            }
        });
    }

    /**
     * Initialise joyStick
     */
    private void initLeftJoyStick() {
        Joystick joystick = findViewById(R.id.leftJoystick);
        final TextView textView = findViewById(R.id.textView);
        final TextView textView1 = findViewById(R.id.textView2);
        final TextView textView2 = findViewById(R.id.textView3);
        final TextView textView3 = findViewById(R.id.textView4);

        final float maximumSpeed = Float.valueOf(getPreferenceValue(6));
        final float minimumSpeed = Float.valueOf(getPreferenceValue(5));

        joystick.setJoystickListener(new JoystickListener() {

            @Override
            public void onDown() {
            }

            @Override
            public void onDrag(float degrees, float offset) {
                if (degrees <= 90.0 && degrees >= 0.0) {
                    degrees = (degrees - 90.0f) * -1.0f;

                    x = (float) (offset * Math.sin(Math.toRadians(degrees)));
                    y = (float) (offset * Math.cos(Math.toRadians(degrees)));

                    offset *= maximumSpeed;
                } else if (degrees < 0.0 && degrees >= -90.0) {
                    degrees *= -1;

                    x = (float) (offset * Math.cos(Math.toRadians(degrees)));
                    y = (float) (offset * Math.sin(Math.toRadians(degrees))) * -1;

                    offset *= minimumSpeed;
                } else if (degrees < -90.0 && degrees >= -180.0) {
                    degrees = (degrees + 90) * -1;

                    x = (float) (offset * Math.sin(Math.toRadians(degrees))) * -1;
                    y = (float) (offset * Math.cos(Math.toRadians(degrees))) * -1;

                    offset *= minimumSpeed;
                } else if (degrees > 90.0){
                    degrees = 180 - degrees;

                    x = (float) (offset * Math.cos(Math.toRadians(degrees))) * -1;
                    y = (float) (offset * Math.sin(Math.toRadians(degrees)));

                    offset *= maximumSpeed;
                }

                textView.setText(String.valueOf(x));
                textView1.setText(String.valueOf(y));
                textView2.setText(String.valueOf(degrees));
                textView3.setText(String.valueOf(offset));
            }

            @Override
            public void onUp() {
                textView.setText("");
                textView1.setText("");
                textView2.setText("");
                textView3.setText("");

                x = 0.0f;
                y = 0.0f;
            }
        });
    }

    private void initRightJoyStick() {
        Joystick joystick = findViewById(R.id.rightJoystick);
        final TextView joyStickValue = findViewById(R.id.textView_rightJoyStickValue);

        joystick.setJoystickListener(new JoystickListener() {

            @Override
            public void onDown() {}

            @Override
            public void onDrag(float degrees, float offset) {
                float pAngularVelocity = Float.parseFloat(getPreferenceValue(3));
                float nAngularVelocity = Float.parseFloat(getPreferenceValue(4));

                if (degrees == -180) {
                    rot_z = offset * pAngularVelocity;
                }

                if (degrees == 0) {
                    rot_z = -offset * nAngularVelocity;
                }

                joyStickValue.setText(String.valueOf(rot_z));
            }

            @Override
            public void onUp() {
                rot_z = 0.0f;
                joyStickValue.setText(String.valueOf(rot_z));
            }
        });
    }

    /**
     * Starts socket service
     */
    private void startSocketService() {
        TextView textView = findViewById(R.id.editText_ipAddress);
        String ipAddress = String.valueOf(textView.getText());

        if (mBound) {
            socketService.openSocket(ipAddress, Integer.parseInt(getPreferenceValue(0)));
        }
    }

// -------------------------------------------------------------------------------------------------

// Tab 2 -------------------------------------------------------------------------------------------

    private void initDynamicGraph() {
        // we get graph view instance
        GraphView graph = findViewById(R.id.graph);
        // data
        series = new LineGraphSeries<>();
        graph.addSeries(series);
        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(10);
        viewport.setScrollable(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // we're going to simulate real time with thread that append data to the graph
        new Thread(new Runnable() {

            @Override
            public void run() {
                // we add 100 new entries
                for (int i = 0; i < 100; i++) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            addEntry();
                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        // manage error ...
                    }
                }
            }
        }).start();
    }

    // add random data to graph
    private void addEntry() {
        // here, we choose to display max 10 points on the viewport and we scroll to end
        series.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 10d), true, 10);
    }

    private void initSpinner(){
        Spinner spinner = findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.engines, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

// -------------------------------------------------------------------------------------------------

// Callbacks interface implementation --------------------------------------------------------------

    @Override
    public boolean getToggleButtonStatus() {
        ToggleButton toggleButton = findViewById(R.id.toggleButton_connection);
        return toggleButton.isChecked();
    }

    @Override
    public ControlData getControlData() {
        ControlData controlData = new ControlData();

        controlData.setAngularVelocity(rot_z);
        controlData.setX(x);
        controlData.setY(y);

        return controlData;
    }

    @Override
    public void hostErrorHandler() {
        String errorMessage = getString(R.string.error_msg_host_error);
        // TODO fix exception
        // unCheckAllConnectionButtons();
        //Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

//--------------------------------------------------------------------------------------------------
}