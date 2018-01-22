package com.mobileapplicationdev.roboproject;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Florian on 16.01.2018.
 * Settings activity for port numbers
 */

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
