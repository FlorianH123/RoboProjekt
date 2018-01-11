package com.roboprojekt.mobileapplicationdev.roboprojekt;

import android.content.ClipData;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addItemOnDebugSpinner();
        addListenerToDebugSpinner();
    }

    public void onConnectionButtonClick(View view) {
        ToggleButton toggle = findViewById(R.id.toggleButton_connection);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //TODO Socket starten
                    Toast.makeText(MainActivity.this, "Activated" , Toast.LENGTH_SHORT).show();
                } else {
                    // TODO Socket schließen
                    Toast.makeText(MainActivity.this, "Deactivated" , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addItemOnDebugSpinner() {
        Spinner spinner = findViewById(R.id.spinner_debug_view);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_debug_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }

    private void addListenerToDebugSpinner() {
        Spinner spinner = findViewById(R.id.spinner_debug_view);
        spinner.setOnItemSelectedListener(new DebugItemSelectedListener());
    }
}
