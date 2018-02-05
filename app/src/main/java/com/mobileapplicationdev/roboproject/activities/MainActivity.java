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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;
import com.mobileapplicationdev.roboproject.R;
import com.mobileapplicationdev.roboproject.models.ControlData;
import com.mobileapplicationdev.roboproject.services.SocketService;
import com.mobileapplicationdev.roboproject.utils.Utils;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SocketService.Callbacks {
    public static final String TAG_TAB_1 = "Tag_Tab1";
    public static final String TAG_TAB_2 = "Tag_Tab2";
    public static final String TAG_TAB_3 = "Tag_Tab3";

    private SocketService socketService;
    private boolean mBound = false;

    private float x;
    private float y;
    private float rot_z = 0.0f;

    /*************************************************************/
    private float xTest = 0f;
    private LineChart realTimeChart;
    /***graph stuff **********************************************/

    private final Random RANDOM = new Random();
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
        initConnectionButtonTab1();
        initConnectionButtonTab2();
        initConnectionButtonTab3();
        initLeftJoyStick();
        initRightJoyStick();
        initDebugToggleButton();
        initDynamicGraph();
        initSpinnerTab2();
        initSpinnerTab3();
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

// -------------------------------------------------------------------------------------------------

// Socket connection -------------------------------------------------------------------------------

    private void unCheckAllConnectionButtons() {
        ToggleButton connectionButtonTab1 = findViewById(R.id.toggleButton_connection_tab1);
        ToggleButton connectionButtonTab2 = findViewById(R.id.toggleButton_connection_tab2);
        ToggleButton connectionButtonTab3 = findViewById(R.id.toggleButton_connection_tab3);

        connectionButtonTab1.setChecked(false);
        connectionButtonTab2.setChecked(false);
        connectionButtonTab3.setChecked(false);
    }

    /**
     * Initialise connection button
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
        TextView textView;
        String ipAddress;

        if (mBound) {
            if (tagTab.equals(TAG_TAB_1)) {
                textView = findViewById(R.id.editText_ipAddress_tab1);
                ipAddress = String.valueOf(textView.getText());

                socketService.openSteeringSocket(ipAddress,
                        Integer.parseInt(getPreferenceValue(0)));
            }

            if (tagTab.equals(TAG_TAB_2)) {
                textView = findViewById(R.id.editText_ipAddress_tab2);
                ipAddress = String.valueOf(textView.getText());

                socketService.openPlottingSocket(ipAddress,
                        Integer.parseInt(getPreferenceValue(1)));
            }

            if (tagTab.equals(TAG_TAB_3)) {
                textView = findViewById(R.id.editText_ipAddress_tab3);
                ipAddress = String.valueOf(textView.getText());

                socketService.openRotatingEngineSocket(ipAddress,
                        Integer.parseInt(getPreferenceValue(2)));
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

    private void initConnectionButtonTab1() {
        ToggleButton toggleButton = findViewById(R.id.toggleButton_connection_tab1);
        EditText editText = findViewById(R.id.editText_ipAddress_tab1);

        initConnectionButton(toggleButton , editText, TAG_TAB_1);
    }

    /**
     * Initialise joyStick
     */
    private void initLeftJoyStick() {
        Joystick joystick = findViewById(R.id.leftJoystick);
        final TextView textViewX = findViewById(R.id.textView_leftJoyStick_X_Value);
        final TextView textViewY = findViewById(R.id.textView_leftJoyStick_Y_Value);

        joystick.setJoystickListener(new JoystickListener() {

            @Override
            public void onDown() {}

            @Override
            public void onDrag(float angle, float offset) {

                float maximumY = Float.valueOf(getPreferenceValue(4));
                float maximumX  = Float.valueOf(getPreferenceValue(5));

                if (Utils.isInFirstQuarter(angle)) {                                  // 1. quarter
                    angle = -(angle - 90);
                    angle = (float) Math.toRadians(angle);

                    y = (float) -(offset * Math.sin(angle) * maximumY);
                    x = (float)  (offset * Math.cos(angle) * maximumX);

                } else if (Utils.isInSecondQuarter(angle)) {                          // 2. quarter
                    angle = (float) Math.toRadians(-angle);

                    y = (float) -(offset * Math.cos(angle) * maximumY);
                    x = (float) -(offset * Math.sin(angle) * maximumX);

                } else if (Utils.isInThirdQuarter(angle)) {                           // 3. quarter
                    angle = -(angle + 90);
                    angle = (float) Math.toRadians(angle);

                    y = (float)  (offset * Math.sin(angle) * maximumY);
                    x = (float) -(offset * Math.cos(angle) * maximumX);

                } else if (Utils.isInFourthQuarter(angle)){                           // 4. quarter
                    angle = 180 - angle;
                    angle = (float) Math.toRadians(angle);

                    y = (float) (offset * Math.cos(angle) * maximumY);
                    x = (float) (offset * Math.sin(angle) * maximumX);
                }

                textViewX.setText(String.format(Locale.getDefault(),"%.2f", x));
                textViewY.setText(String.format(Locale.getDefault(),"%.2f", y));
            }

            @Override
            public void onUp() {
                String defaultValue = getString(R.string.textView_default_value);
                textViewX.setText(defaultValue);
                textViewY.setText(defaultValue);

                x = 0.0f;
                y = 0.0f;
            }
        });
    }

    /**
     * Initialise joyStick
     */
    private void initRightJoyStick() {
        Joystick joystick = findViewById(R.id.rightJoystick);
        final TextView joyStickValue = findViewById(R.id.textView_rightJoyStickValue);

        joystick.setJoystickListener(new JoystickListener() {

            @Override
            public void onDown() {}

            @Override
            public void onDrag(float degrees, float offset) {
                float angularVelocity = Float.parseFloat(getPreferenceValue(3));

                if (degrees == -180) {
                    rot_z = offset * angularVelocity;
                }

                if (degrees == 0) {
                    rot_z = -offset * angularVelocity;
                }

                joyStickValue.setText(String.format(Locale.getDefault(), "%.2f", rot_z));
            }

            @Override
            public void onUp() {
                String defaultValue = getString(R.string.textView_default_value);
                joyStickValue.setText(defaultValue);

                rot_z = 0.0f;
            }
        });
    }

// -------------------------------------------------------------------------------------------------

// TAB 2 -------------------------------------------------------------------------------------------

    private void initConnectionButtonTab2() {
        ToggleButton toggleButton = findViewById(R.id.toggleButton_connection_tab2);
        EditText editText = findViewById(R.id.editText_ipAddress_tab2);

        initConnectionButton(toggleButton , editText, TAG_TAB_2);
    }

    private void initDynamicGraph(){
        realTimeChart = (LineChart)findViewById(R.id.graph);
        //enable description text
        realTimeChart.getDescription().setEnabled(true);
        //enable touch gesture
        realTimeChart.setTouchEnabled(true);
        // enable scaling and dragging
        realTimeChart.setDragEnabled(true);
        realTimeChart.setScaleEnabled(true);
        realTimeChart.setDrawGridBackground(false);

        //realTimeChart.setNoDataText("No data for the moment");

        // if disabled, scaling can be done on x- and y-axis separately
        realTimeChart.setPinchZoom(true);

        // set an alternative background color
        realTimeChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.GREEN);
        //adding clear data
        realTimeChart.setData(data);
        Legend legend = realTimeChart.getLegend();

        // modify the legend ...
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.WHITE);

        XAxis xl = realTimeChart.getXAxis();
        xl.setTextColor(Color.BLUE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);


        YAxis leftAxis = realTimeChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLUE);
        leftAxis.setAxisMaximum(2f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = realTimeChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void initDebugToggleButton(){
        final ToggleButton toggle = findViewById(R.id.toggleButton_debug);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                feedMultiple();
            }
        });
    }

    private void addEntry(){
        //reedit default data
        LineData data = realTimeChart.getData();

        if (data != null){
            ILineDataSet set = data.getDataSetByIndex(0);
            if(set == null){
                set = createSet();
                data.addDataSet(set);
            }
            float herz = Utils.getTimeX(10f);
            //add entry to the data set
            data.addEntry(new Entry(set.getEntryCount(),(float) (Math.random() * 1.5) ), 0);
            data.notifyDataChanged();
            xTest=+herz;

            // let the chart know it's data has changed
            realTimeChart.notifyDataSetChanged();

            // limit the number of visible entries
            realTimeChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            realTimeChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }
    private Thread thread;

    private void feedMultiple() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private Double[] testStrings(){
        Double[] array= new Double[10000];

        for(int i = 0;i<10000;i++){
            array[i] = (RANDOM.nextDouble() *1.5);
        }
        return array;
    }

    private void initSpinnerTab2(){
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

// TAB 3 -------------------------------------------------------------------------------------------

    private void initConnectionButtonTab3() {
        ToggleButton toggleButton = findViewById(R.id.toggleButton_connection_tab3);
        EditText editText =  findViewById(R.id.editText_ipAddress_tab3);

        initConnectionButton(toggleButton, editText, TAG_TAB_3);
    }

    private void initSpinnerTab3(){
        Spinner spinner = findViewById(R.id.spinner_engines_tab3);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.engines, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    private void startDebugSocket(){
        TextView textView = findViewById(R.id.editText_ipAddress_tab2);
        String ipAdress = String.valueOf(textView.getText());

        if(mBound){
            socketService.openPlottingSocket(ipAdress,Integer.parseInt(getPreferenceValue(1)));
        }
    }

// -------------------------------------------------------------------------------------------------

// Callbacks interface implementation --------------------------------------------------------------

    @Override
    public boolean getToggleButtonStatus() {
        ToggleButton toggleButton = findViewById(R.id.toggleButton_connection_tab1);
        return toggleButton.isChecked();
    }

    @Override
    public boolean getDebugButtonStatus(){
        ToggleButton toggleButton = findViewById(R.id.toggleButton_debug);
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
    public ControlData setControlDataDebug(){
        TextView enterDebugSpeed = findViewById(R.id.editText_enterDebug_speed);
        TextView enterVarI = findViewById(R.id.editTextEnterI);
        TextView enterVarP = findViewById(R.id.editTextEnterP);
        TextView enterRegulatorFrequency = findViewById(R.id.editTextFrequency);

        ControlData controlData = new ControlData();

        controlData.setSpeed(Integer.parseInt(enterDebugSpeed.getText().toString()));
        controlData.setVarI(Float.parseFloat(enterVarI.getText().toString()));
        controlData.setVarP(Float.parseFloat(enterVarP.getText().toString()));
        controlData.setRegulatorFrequency(Float.parseFloat(
                enterRegulatorFrequency.getText().toString()));

        return controlData;
    }

    public ToggleButton getToggleButton(String tagTab) {
        switch (tagTab) {
            case TAG_TAB_1:
                return findViewById(R.id.toggleButton_connection_tab1);
            case TAG_TAB_2:
                return findViewById(R.id.toggleButton_connection_tab2);
            case TAG_TAB_3:
                return findViewById(R.id.toggleButton_connection_tab3);
            default:
                return null;
        }
    }
//--------------------------------------------------------------------------------------------------
}