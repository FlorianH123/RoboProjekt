package com.mobileapplicationdev.roboproject.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;
import com.mobileapplicationdev.roboproject.R;
import com.mobileapplicationdev.roboproject.db.DatabaseHelper;
import com.mobileapplicationdev.roboproject.models.ControlData;
import com.mobileapplicationdev.roboproject.models.RobotProfile;
import com.mobileapplicationdev.roboproject.services.SocketService;
import com.mobileapplicationdev.roboproject.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SocketService.Callbacks {
    private ToggleButton connectionButtonTab1;
    private ToggleButton connectionButtonTab2;
    private ToggleButton connectionButtonTab3;
    private ToggleButton debugButtonTab2;
    private ToggleButton debugButtonTab3;
    private ToggleButton graphButtonTab2;
    private ToggleButton graphButtonTab3;

    private EditText ipAddressTextFieldTab1;
    private EditText ipAddressTextFieldTab2;
    private EditText ipAddressTextFieldTab3;
    private EditText editFrequencyTab2;
    private EditText iValueTextFieldTab2;
    private EditText pValueTextFieldTab2;
    private EditText velocityTextField;
    private EditText editFrequencyTab3;
    private EditText iValueTextFieldTab3;
    private EditText pValueTextFieldTab3;
    private EditText angleTextField;

    private Joystick leftJoyStick;
    private Joystick rightJoyStick;

    private TextView leftJoyStick_X_Value;
    private TextView leftJoyStick_Y_Value;
    private TextView rightJoyStick_Value;

    private LineChart debugVelocityChart;
    private LineChart debugAngleChart;

    private Button resetButtonTab2;
    private Button resetButtonTab3;

    private Spinner engineSpinnerTab2;
    private Spinner engineSpinnerTab3;

    public static final String TAG_TAB_0 = "Tag_Tab0";
    public static final String TAG_TAB_1 = "Tag_Tab1";
    public static final String TAG_TAB_2 = "Tag_Tab2";
    public static final String TAG_TAB_3 = "Tag_Tab3";

    public static final int TAB_ID_1 = 1;
    public static final int TAB_ID_2 = 2;
    public static final int TAB_ID_3 = 3;

    private final Object waiter = new Object();

    private SocketService socketService;
    private boolean mBound = false;

    private float x;
    private float y;
    private float rot_z = 0.0f;

    private String dbIpAddress;
    private DatabaseHelper dbh;

    private LineDataSet.Mode graphModeTab2 = LineDataSet.Mode.CUBIC_BEZIER;
    private LineDataSet.Mode graphModeTab3 = LineDataSet.Mode.CUBIC_BEZIER;

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
        //TODO da knallt er
        setSupportActionBar(toolbar);

        dbh = new DatabaseHelper(this);
        dbIpAddress = dbh.getIp();

        // Initialise components inside  the main activity
        initAllComponents();
        initTabHost();
        initConnectionButtonTab1();
        initConnectionButtonTab2();
        initConnectionButtonTab3();
        initLeftJoyStick();
        initRightJoyStick();
        initDebugToggleButtonTab2();
        initDebugToggleButtonTab3();
        initGraphTab2();
        initGraphTab3();
        initSpinnerTab2();
        initSpinnerTab3();
        initResetButtonTab2();
        initResetButtonTab3();
        initGraphToggleButtonTab2();
        initGraphToggleButtonTab3();
    }

    /**
     * Get all components from xml
     */
    private void initAllComponents() {
        connectionButtonTab1 = findViewById(R.id.toggleButton_connection_tab1);
        connectionButtonTab2 = findViewById(R.id.toggleButton_connection_tab2);
        connectionButtonTab3 = findViewById(R.id.toggleButton_connection_tab3);
        debugButtonTab2 = findViewById(R.id.toggleButton_debug_tab2);
        debugButtonTab3 = findViewById(R.id.toggleButton_debug_tab3);
        graphButtonTab2 = findViewById(R.id.toggleButton_graph_tab2);
        graphButtonTab3 = findViewById(R.id.toggleButton_graph_tab3);

        ipAddressTextFieldTab1 = findViewById(R.id.editText_ipAddress_tab1);
        ipAddressTextFieldTab2 = findViewById(R.id.editText_ipAddress_tab2);
        ipAddressTextFieldTab3 = findViewById(R.id.editText_ipAddress_tab3);
        editFrequencyTab2 = findViewById(R.id.editTextFrequency);
        iValueTextFieldTab2 = findViewById(R.id.editTextEnterI);
        pValueTextFieldTab2 = findViewById(R.id.editTextEnterP);
        velocityTextField = findViewById(R.id.editText_enterDebug_speed);
        editFrequencyTab3 = findViewById(R.id.editText_frequency_tab3);
        iValueTextFieldTab3 = findViewById(R.id.editText_i_tab3);
        pValueTextFieldTab3 = findViewById(R.id.editText_p_tab3);
        angleTextField = findViewById(R.id.editText_angle_tab3);

        leftJoyStick = findViewById(R.id.leftJoystick);
        rightJoyStick = findViewById(R.id.rightJoystick);

        leftJoyStick_X_Value = findViewById(R.id.textView_leftJoyStick_X_Value);
        leftJoyStick_Y_Value = findViewById(R.id.textView_leftJoyStick_Y_Value);
        rightJoyStick_Value = findViewById(R.id.textView_rightJoyStickValue);

        debugVelocityChart = findViewById(R.id.graph_tab2);
        debugAngleChart = findViewById(R.id.graph_tab3);

        resetButtonTab2 = findViewById(R.id.button_reset_tab2);
        resetButtonTab3 = findViewById(R.id.button_reset_tab3);

        engineSpinnerTab2 = findViewById(R.id.spinner);
        engineSpinnerTab3 = findViewById(R.id.spinner_engines_tab3);
    }

    /**
     * Returns the value of a shared preference
     *
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

    /**
     * Initialise tabHost with three different tabs
     */
    private void initTabHost() {
        String tabNameTab0 = "Profile";
        String tabNameTab1 = getString(R.string.tab_name_tab1);
        String tabNameTab2 = getString(R.string.tab_name_tab2);
        String tabNameTab3 = getString(R.string.tab_name_tab3);

        TabHost th = findViewById(R.id.tabHost);
        th.setup();

        TabHost.TabSpec specs = th.newTabSpec(TAG_TAB_0);
        specs.setContent(R.id.tab0);
        specs.setIndicator(tabNameTab0);
        th.addTab(specs);

        // TAB 1
        specs = th.newTabSpec(TAG_TAB_1);
        specs.setContent(R.id.tab1);
        specs.setIndicator(tabNameTab1);
        th.addTab(specs);

        // TAB 2
        specs = th.newTabSpec(TAG_TAB_2);
        specs.setContent(R.id.tab2);
        specs.setIndicator(tabNameTab2);
        th.addTab(specs);

        // TAB 2
        specs = th.newTabSpec(TAG_TAB_3);
        specs.setContent(R.id.tab3);
        specs.setIndicator(tabNameTab3);
        th.addTab(specs);

        // On tab change listener
        th.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tagName) {
                unCheckAllConnectionButtons();
            }
        });
    }

    /**
     * Init data graph for debug views
     * @param realTimeChart the graph for the actual tab
     */
    private void initDynamicGraph(LineChart realTimeChart) {
        LineData data = new LineData();
        Legend legend;
        XAxis xAxis;
        YAxis left_Y_Axis;
        YAxis right_Y_Axis;

        //enable description text
        realTimeChart.getDescription().setEnabled(false);

        //enable touch gesture
        realTimeChart.setTouchEnabled(true);

        // enable scaling and dragging
        realTimeChart.setDragEnabled(true);
        realTimeChart.setScaleEnabled(true);
        realTimeChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        realTimeChart.setPinchZoom(false);

        // set an alternative background color
        realTimeChart.setBackgroundColor(Color.TRANSPARENT);

        //adding clear data
        realTimeChart.setData(data);
        legend = realTimeChart.getLegend();

        // modify the legend ...
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);

        xAxis = realTimeChart.getXAxis();
        xAxis.setTextColor(Color.BLUE);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setEnabled(true);

        left_Y_Axis = realTimeChart.getAxisLeft();
        left_Y_Axis.setTextColor(Color.BLUE);
        left_Y_Axis.setAxisMaximum(2f);
        left_Y_Axis.setAxisMinimum(0f);
        left_Y_Axis.setDrawGridLines(true);

        right_Y_Axis = realTimeChart.getAxisRight();
        right_Y_Axis.setEnabled(false);
    }

// -------------------------------------------------------------------------------------------------

// Socket connection -------------------------------------------------------------------------------

    /**
     * On tab switch toggle all connection buttons
     */
    private void unCheckAllConnectionButtons() {
        connectionButtonTab1.setChecked(false);
        connectionButtonTab2.setChecked(false);
        connectionButtonTab3.setChecked(false);
    }

    /**
     * Initialise connection buttons
     */
    private void initConnectionButton(final ToggleButton toggle, final EditText editText_ipAddress,
                                      final String tagTab) {

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String ipAddress;
                String errMsgInvalidIp = getString(R.string.error_msg_invalid_ip);

                if (isChecked) {
                    ipAddress = String.valueOf(editText_ipAddress.getText());

                    if (!ipAddress.trim().isEmpty()) {
                        editText_ipAddress.setEnabled(false);
                        startSocketService(tagTab);

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
     * Get a communication interface for the started socket service
     */
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
     * Starts socket service
     */
    private void startSocketService(String tagTab) {
        String ipAddress;

        if (mBound) {
            if (tagTab.equals(TAG_TAB_1)) {

                ipAddress = String.valueOf(ipAddressTextFieldTab1.getText());

                socketService.openSteeringSocket(ipAddress,
                        Integer.parseInt(getPreferenceValue(0)));
            }

            if (tagTab.equals(TAG_TAB_2)) {
                ipAddress = String.valueOf(ipAddressTextFieldTab2.getText());

                socketService.openDebugSocket(ipAddress,
                        Integer.parseInt(getPreferenceValue(1)), waiter, TAB_ID_2);
            }

            if (tagTab.equals(TAG_TAB_3)) {
                ipAddress = String.valueOf(ipAddressTextFieldTab3.getText());

                socketService.openDebugSocket(ipAddress,
                        Integer.parseInt(getPreferenceValue(2)), waiter, TAB_ID_3);
            }
        }
    }

// -------------------------------------------------------------------------------------------------

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

    /**
     * Init connection button for tab1
     */
    private void initConnectionButtonTab1() {
        dbIpAddress = dbh.getIp();
        ipAddressTextFieldTab1.setText(dbIpAddress);

        initConnectionButton(connectionButtonTab1, ipAddressTextFieldTab1, TAG_TAB_1);
    }

    /**
     * Initialise joyStick
     */
    private void initLeftJoyStick() {
        leftJoyStick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
            }

            @Override
            public void onDrag(float angle, float offset) {

                float maximumY = Float.valueOf(getPreferenceValue(4));
                float maximumX = Float.valueOf(getPreferenceValue(5));

                if (Utils.isInFirstQuarter(angle)) {                                  // 1. quarter
                    angle = -(angle - 90);
                    angle = (float) Math.toRadians(angle);

                    y = (float) -(offset * Math.sin(angle) * maximumY);
                    x = (float) (offset * Math.cos(angle) * maximumX);

                } else if (Utils.isInSecondQuarter(angle)) {                          // 2. quarter
                    angle = (float) Math.toRadians(-angle);

                    y = (float) -(offset * Math.cos(angle) * maximumY);
                    x = (float) -(offset * Math.sin(angle) * maximumX);

                } else if (Utils.isInThirdQuarter(angle)) {                           // 3. quarter
                    angle = -(angle + 90);
                    angle = (float) Math.toRadians(angle);

                    y = (float) (offset * Math.sin(angle) * maximumY);
                    x = (float) -(offset * Math.cos(angle) * maximumX);

                } else if (Utils.isInFourthQuarter(angle)) {                           // 4. quarter
                    angle = 180 - angle;
                    angle = (float) Math.toRadians(angle);

                    y = (float) (offset * Math.cos(angle) * maximumY);
                    x = (float) (offset * Math.sin(angle) * maximumX);
                }

                leftJoyStick_X_Value.setText(String.format(Locale.getDefault(), "%.2f", x));
                leftJoyStick_Y_Value.setText(String.format(Locale.getDefault(), "%.2f", y));
            }

            @Override
            public void onUp() {
                String defaultValue = getString(R.string.textView_default_value);
                leftJoyStick_X_Value.setText(defaultValue);
                leftJoyStick_Y_Value.setText(defaultValue);

                x = 0.0f;
                y = 0.0f;
            }
        });
    }

    /**
     * Initialise joyStick
     */
    private void initRightJoyStick() {
        rightJoyStick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
            }

            @Override
            public void onDrag(float degrees, float offset) {
                float angularVelocity = Float.parseFloat(getPreferenceValue(3));

                if (degrees == -180) {
                    rot_z = offset * angularVelocity;
                }

                if (degrees == 0) {
                    rot_z = -offset * angularVelocity;
                }

                rightJoyStick_Value.setText(String.format(Locale.getDefault(), "%.2f", rot_z));
            }

            @Override
            public void onUp() {
                String defaultValue = getString(R.string.textView_default_value);
                rightJoyStick_Value.setText(defaultValue);

                rot_z = 0.0f;
            }
        });
    }

// -------------------------------------------------------------------------------------------------

// TAB 2 -------------------------------------------------------------------------------------------

    /**
     * Init connection button for tab 2
     */
    private void initConnectionButtonTab2() {
        dbIpAddress = dbh.getIp();
        ipAddressTextFieldTab2.setText(dbIpAddress);

        initConnectionButton(connectionButtonTab2, ipAddressTextFieldTab2, TAG_TAB_2);
    }

    /**
     * Init debug graph for tab 2
     */
    private void initGraphTab2() {
        initDynamicGraph(debugVelocityChart);
    }

    private void initResetButtonTab2() {
        resetButtonTab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugVelocityChart.clearValues();
                debugVelocityChart.invalidate();
                debugVelocityChart.clear();
                initDynamicGraph(debugVelocityChart);
            }
        });
    }

    /**
     * Init debug toggle button for tab 2
     */
    private void initDebugToggleButtonTab2() {
        debugButtonTab2.setEnabled(false);

        debugButtonTab2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                String errMsgInvalidInput = getString(R.string.error_msg_invalid_debug_input);
                String iValue = String.valueOf(iValueTextFieldTab2.getText());
                String pValue = String.valueOf(ipAddressTextFieldTab2.getText());
                String frequency = String.valueOf(editFrequencyTab2.getText());
                String velocity = String.valueOf(velocityTextField.getText());

                if (isChecked) {
                    if (Utils.validateInput(iValue, pValue, frequency, velocity)) {
                        debugButtonTab2.setChecked(false);
                        Toast.makeText(MainActivity.this,
                                errMsgInvalidInput, Toast.LENGTH_SHORT).show();
                    } else {
                        connectionButtonTab2.setEnabled(false);
                        ipAddressTextFieldTab2.setEnabled(false);
                        editFrequencyTab2.setEnabled(false);
                        iValueTextFieldTab2.setEnabled(false);
                        pValueTextFieldTab2.setEnabled(false);
                        velocityTextField.setEnabled(false);

                        // Notify debug socket thread p and i value are updated
                        synchronized (waiter) {
                            waiter.notify();
                        }
                    }
                } else {
                    connectionButtonTab2.setEnabled(true);
                    connectionButtonTab2.setChecked(false);
                    debugButtonTab2.setEnabled(false);
                    ipAddressTextFieldTab2.setEnabled(true);
                    editFrequencyTab2.setEnabled(true);
                    iValueTextFieldTab2.setEnabled(true);
                    pValueTextFieldTab2.setEnabled(true);
                    velocityTextField.setEnabled(true);
                }
            }
        });
    }

    /**
     * Init engine spinner tab 2
     */
    private void initSpinnerTab2() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.engines, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        engineSpinnerTab2.setAdapter(adapter);
    }

    /**
     * Init graph toggle button to change between cubic graph and linear graph
     */
    private void initGraphToggleButtonTab2() {
        graphButtonTab2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    graphModeTab2 = LineDataSet.Mode.LINEAR;
                } else {
                    graphModeTab2 = LineDataSet.Mode.CUBIC_BEZIER;
                }
            }
        });
    }

// -------------------------------------------------------------------------------------------------

// TAB 3 -------------------------------------------------------------------------------------------

    /**
     * Init reset chart button for tab 3
     */
    private void initResetButtonTab3() {
        resetButtonTab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugAngleChart.clearValues();
                debugAngleChart.invalidate();
                debugAngleChart.clear();
                initDynamicGraph(debugAngleChart);
            }
        });
    }

    /**
     * Init connection button for tab 3
     */
    private void initConnectionButtonTab3() {
        dbIpAddress = dbh.getIp();
        ipAddressTextFieldTab3.setText(dbIpAddress);

        initConnectionButton(connectionButtonTab3, ipAddressTextFieldTab3, TAG_TAB_3);
    }

    /**
     * Init data graph for tab 3
     */
    private void initGraphTab3() {
        initDynamicGraph(debugAngleChart);
    }

    /**
     * Init debug toggle button for tab 3
     */
    private void initDebugToggleButtonTab3() {
        debugButtonTab3.setEnabled(false);

        debugButtonTab3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                String errMsgInvalidInput = getString(R.string.error_msg_invalid_debug_input);

                if (isChecked) {
                    String iValue = String.valueOf(iValueTextFieldTab3.getText());
                    String pValue = String.valueOf(ipAddressTextFieldTab3.getText());
                    String frequency = String.valueOf(editFrequencyTab3.getText());
                    String velocity = String.valueOf(angleTextField.getText());

                    if (Utils.validateInput(iValue, pValue, frequency, velocity)) {
                        debugButtonTab3.setChecked(false);
                        Toast.makeText(MainActivity.this,
                                errMsgInvalidInput, Toast.LENGTH_SHORT).show();
                    } else {
                        connectionButtonTab3.setEnabled(false);
                        ipAddressTextFieldTab3.setEnabled(false);
                        editFrequencyTab3.setEnabled(false);
                        iValueTextFieldTab3.setEnabled(false);
                        pValueTextFieldTab3.setEnabled(false);
                        angleTextField.setEnabled(false);

                        // Notify debug socket thread p and i value are updated
                        synchronized (waiter) {
                            waiter.notify();
                        }
                    }
                } else {
                    connectionButtonTab3.setEnabled(true);
                    connectionButtonTab3.setChecked(false);
                    debugButtonTab3.setEnabled(false);
                    ipAddressTextFieldTab3.setEnabled(true);
                    editFrequencyTab3.setEnabled(true);
                    iValueTextFieldTab3.setEnabled(true);
                    pValueTextFieldTab3.setEnabled(true);
                    angleTextField.setEnabled(true);
                }
            }
        });
    }

    /**
     * Init engine spinner for tab 3
     */
    private void initSpinnerTab3() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.engines, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        engineSpinnerTab3.setAdapter(adapter);
    }

    /**
     * Init graph toggle button to change between cubic graph and linear graph
     */
    private void initGraphToggleButtonTab3() {
        graphButtonTab3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    graphModeTab3 = LineDataSet.Mode.LINEAR;
                } else {
                    graphModeTab3 = LineDataSet.Mode.CUBIC_BEZIER;
                }
            }
        });
    }

// -------------------------------------------------------------------------------------------------

// Callbacks interface implementation --------------------------------------------------------------

    /**
     * Returns the graph for the actual tab
     * @param tabId the id of the tab (1,2,3)
     * @return the graph
     */
    @Override
    public LineChart getLineChart(int tabId) {
        if (tabId == MainActivity.TAB_ID_2) {
            return debugVelocityChart;
        } else if (tabId == MainActivity.TAB_ID_3) {
            return debugAngleChart;
        }

        return null;
    }

    /**
     * Returns the connection button for tab 1
     * @return connection button
     */
    @Override
    public boolean getConnectionButtonStatus() {
        return connectionButtonTab1.isChecked();
    }

    /**
     * Returns if the button of the actual tab is checked
     * @param tabId tab id
     * @return true if the button is checked, otherwise false
     */
    @Override
    public boolean getDebugButtonStatus(int tabId) {
        if (tabId == TAB_ID_2) {
            return debugButtonTab2.isChecked();
        }

        return tabId == TAB_ID_3 && debugButtonTab3.isChecked();
    }

    /**
     * Set the p value for the text field in the actual tab
     * @param p p value
     * @param tabId tab id
     */
    @Override
    public void setP(final float p, int tabId) {
        if (tabId == TAB_ID_2) {
            pValueTextFieldTab2.post(new Runnable() {
                @Override
                public void run() {
                    pValueTextFieldTab2.setText(String.valueOf(p));
                }
            });
        }

        if (tabId == TAB_ID_3) {
            pValueTextFieldTab3.post(new Runnable() {
                @Override
                public void run() {
                    pValueTextFieldTab3.setText(String.valueOf(p));
                }
            });
        }
    }

    /**
     * Returns the actual value of the p text field
     * @param tabId tab id
     * @return p value
     */
    @Override
    public float getP(int tabId) {
        String pValue = null;

        if (tabId == TAB_ID_2) {
            pValue = pValueTextFieldTab2.getText().toString().trim();
        }

        if (tabId == TAB_ID_3) {
            pValue = pValueTextFieldTab3.getText().toString().trim();
        }

        if (pValue == null || pValue.isEmpty()) {
            Log.e("RobotProject", "P value is empty!");
            return 0f;
        }

        return Float.parseFloat(pValue);
    }

    /**
     * Set the p value for the text field in the actual tab
     * @param i i value
     * @param tabId tab id
     */
    @Override
    public void setI(final float i, int tabId) {
        if (tabId == TAB_ID_2) {
            iValueTextFieldTab2.post(new Runnable() {
                @Override
                public void run() {
                    iValueTextFieldTab2.setText(String.valueOf(i));
                }
            });
        }

        if (tabId == TAB_ID_3) {
            iValueTextFieldTab3.post(new Runnable() {
                @Override
                public void run() {
                    iValueTextFieldTab3.setText(String.valueOf(i));
                }
            });
        }
    }

    /**
     * Returns the actual value of the i text field
     * @param tabId tab id
     * @return i value
     */
    @Override
    public float getI(int tabId) {
        String iValue = null;

        if (tabId == TAB_ID_2) {
            iValue = iValueTextFieldTab2.getText().toString().trim();
        }

        if (tabId == TAB_ID_3) {
            iValue = iValueTextFieldTab3.getText().toString().trim();
        }

        if (iValue == null || iValue.isEmpty()) {
            Log.e("RobotProject", "I value is empty!");
            return 0f;
        }

        return Float.parseFloat(iValue);
    }

    /**
     * Returns the frequency of the robot
     * @param tabId tab id
     * @return robot frequency
     */
    @Override
    public float getFrequency(int tabId) {
        if (tabId == TAB_ID_2) {
            return Integer.parseInt(editFrequencyTab2.getText().toString());
        }

        if (tabId == TAB_ID_3) {
            return Integer.parseInt(editFrequencyTab3.getText().toString());
        }

        return 0;
    }

    /**
     * Returns the velocity, located in the velocity text field in tab 2
     * @return velocity
     */
    @Override
    public float getVelocity() {
        String speedValue;
        speedValue = velocityTextField.getText().toString().trim();

        if (speedValue.isEmpty()) {
            Log.e("RobotProject", "Velocity value is empty!");
            return 0f;
        }

        return Float.parseFloat(speedValue);
    }

    /**
     * Returns the angle , located in the angle text field in tab 3
     * @return angle
     */
    @Override
    public int getAngle() {
        String angleValue;
        angleValue = angleTextField.getText().toString().trim();

        if (angleValue.isEmpty()) {
            Log.e("RobotProject", "Angle value is empty!");
            return 0;
        }

        return Integer.parseInt(angleValue);
    }

    /**
     * Returns the toggle button of the actual tab
     * @param tagTab tab name
     * @return toggle button
     */
    @Override
        public ToggleButton getConnectionToggleButton(String tagTab) {
        switch (tagTab) {
            case TAG_TAB_1:
                return connectionButtonTab1;
            case TAG_TAB_2:
                return connectionButtonTab2;
            case TAG_TAB_3:
                return connectionButtonTab3;
            default:
                return null;
        }
    }

    /**
     * Returns the debug toggle button of the actual tab
     * @param tagTab tab name
     * @return debug toggle button
     */
    public ToggleButton getDebugButton(String tagTab) {
        switch (tagTab) {
            case TAG_TAB_2:
                return debugButtonTab2;
            case TAG_TAB_3:
                return debugButtonTab3;
            default:
                return null;
        }
    }

    /**
     * Returns the control data for controlling the robot
     * @return control data
     */
    @Override
    public ControlData getControlData() {
        ControlData controlData = new ControlData();

        controlData.setAngularVelocity(rot_z);
        controlData.setX(x);
        controlData.setY(y);

        return controlData;
    }

    /**
     * Returns the selected engine from the engine spinner
     * @param tabId tab id
     * @return selected engine
     */
    @Override
    public int getSelectedEngine(int tabId) {
        if (tabId == TAB_ID_2) {
            return engineSpinnerTab2.getSelectedItemPosition();
        }

        if (tabId == TAB_ID_3) {
            return engineSpinnerTab3.getSelectedItemPosition();
        }

        return 0;
    }

    /**
     * Enables the debug button
     * @param tabId tab id
     */
    public void enableDebugButton(int tabId) {
        if (tabId == TAB_ID_2) {
            debugButtonTab2.post(new Runnable() {
                @Override
                public void run() {
                    debugButtonTab2.setEnabled(true);
                }
            });
        }

        if (tabId == TAB_ID_3) {
            debugButtonTab3.post(new Runnable() {
                @Override
                public void run() {
                    debugButtonTab3.setEnabled(true);
                }
            });
        }
    }

    public LineDataSet.Mode getGraphMode(int tabId) {
        if (tabId == TAB_ID_2) {
            return graphModeTab2;
        }

        return graphModeTab3;
    }

//--------------------------------------------------------------------------------------------------
}