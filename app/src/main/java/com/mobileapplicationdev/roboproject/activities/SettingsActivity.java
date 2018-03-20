package com.mobileapplicationdev.roboproject.activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mobileapplicationdev.roboproject.R;
import com.mobileapplicationdev.roboproject.db.DatabaseHelper;
import com.mobileapplicationdev.roboproject.models.RobotProfile;

import java.util.ArrayList;

/**
 * Created by Florian on 16.01.2018.
 * Settings activity for port numbers
 */

public class SettingsActivity extends PreferenceActivity {
    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;
    private DatabaseHelper dbh;
    private ListView profileListView;
    private AlertDialog profileDialog;
    private ArrayList<RobotProfile> profileList;
    private ArrayAdapter<RobotProfile> profilesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        initProfileListView();
        dbh = new DatabaseHelper(this);
        initOnPreferenceChange();
        initDialog();

        Preference profileButton = getPreferenceManager().findPreference(getString(R.string.settings_profile_key));
        if (profileButton != null) {
            profileButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    profileDialog.show();
                    return true;
                }
            });
        }
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

                if(key.equals(getString(R.string.settings_ip_key))){
                    defaultValue = dbh.getIp();
                    String newIp = sharedPreferences.getString(key, defaultValue);
                    if(!dbh.updateIp(newIp)){
                        Toast.makeText(SettingsActivity.this, "Ungültige IP konnte nicht gespeichert werden!", Toast.LENGTH_SHORT).show();
                    }
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

    private void initProfileListView() {
        profileListView = new ListView(this);
        loadProfiles();
        profilesAdapter = new ArrayAdapter<>(this, R.layout.profile_list_item, R.id.profileListItem, profileList);
        profileListView.setAdapter(profilesAdapter);
        profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    private void initDialog() {
        AlertDialog.Builder dialogBuilder;

        Log.d("Preference", "true");
        dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.setView(profileListView);
        profileDialog = dialogBuilder.create();
    }

    /**
     * load all profiles from the database and insert them into an array
     */
    private void loadProfiles() {
        profileList = new ArrayList<>();
        //TODO alle Profile aus der Datenbank laden und in die profileList einfügen
        RobotProfile robo = new RobotProfile("Robo", "127.0.0.1", 1, 2,3, 2.0f, 3.0f, 1.0f);
        profileList.add(robo);

    }

    private void addProfile(RobotProfile robotProfile) {
        profileList.add(robotProfile);
        profilesAdapter.notifyDataSetChanged();
    }
}