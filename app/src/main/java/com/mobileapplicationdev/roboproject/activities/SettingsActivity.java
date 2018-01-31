package com.mobileapplicationdev.roboproject.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.mobileapplicationdev.roboproject.R;

/**
 * Created by Florian on 16.01.2018.
 * Settings activity for port numbers
 */

public class SettingsActivity extends PreferenceActivity {
    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        initOnPreferenceChange();
    }

    private void initOnPreferenceChange() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        sp.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                String defaultValue = "0";
                String portNumberString;
                int portNumber;

                if (key.equals(getString(R.string.settings_port_1_key)) ||
                    key.equals(getString(R.string.settings_port_2_key)) ||
                    key.equals(getString(R.string.settings_port_3_key))) {

                    if (key.equals(getString(R.string.settings_port_1_key))) {
                        defaultValue = getString(R.string.port_1_default_value);
                    } else if (key.equals(getString(R.string.settings_port_2_key))) {
                        defaultValue = getString(R.string.port_2_default_value);
                    } else if (key.equals(getString(R.string.settings_port_3_key))) {
                        defaultValue = getString(R.string.port_3_default_value);
                    }

                    portNumberString = sharedPreferences.getString(key, defaultValue);
                    portNumber = Integer.parseInt(portNumberString);

                    validatePortNumber(portNumber, key, defaultValue, sharedPreferences);
                }
            }
        });
    }

    private void validatePortNumber(int portNumber, String key, String defaultValue,
                                    SharedPreferences sharedPreferences) {
        String errorMsg = getString(R.string.error_msg_invalid_port);

        if (portNumber < MIN_PORT || portNumber > MAX_PORT) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, defaultValue);
            editor.apply();

            reset(key, defaultValue);

            Toast.makeText(SettingsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void reset(CharSequence key, String defaultValue) {
        EditTextPreference myPrefText = (EditTextPreference) super.findPreference(key);
        myPrefText.setText(defaultValue);
    }
}