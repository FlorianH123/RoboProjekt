package com.mobileapplicationdev.roboproject.activities;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobileapplicationdev.roboproject.R;
import com.mobileapplicationdev.roboproject.db.DatabaseHelper;
import com.mobileapplicationdev.roboproject.models.RobotProfile;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Florian on 16.01.2018.
 * Settings activity for port numbers
 */

public class SettingsActivity extends PreferenceActivity {
    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;
    private DatabaseHelper dbh;
    private ListView profileListView;
    private AlertDialog profileListDialog;
    private ArrayList<RobotProfile> profileList;
    private ArrayAdapter<RobotProfile> profilesAdapter;
//    AlertDialog.Builder dialogProfileEditBuilder;

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
                    profileListDialog.show();
//                    dialog.show();
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

                if (key.equals(getString(R.string.settings_ip_key))) {
                    defaultValue = dbh.getIp();
                    String newIp = sharedPreferences.getString(key, defaultValue);
                    if (!dbh.updateIp(newIp)) {
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
                RobotProfile robotProfile = (RobotProfile) profileListView.getItemAtPosition(position);
                Log.d("Preference", robotProfile.toString());
                editProfileDialog(robotProfile);
            }
        });
    }

    private void initDialog() {
        AlertDialog.Builder dialogProfileListBuilder;

        dialogProfileListBuilder = new AlertDialog.Builder(SettingsActivity.this);
        dialogProfileListBuilder.setCancelable(true);
        dialogProfileListBuilder.setPositiveButton("Ok", null);
        dialogProfileListBuilder.setView(profileListView);
        profileListDialog = dialogProfileListBuilder.create();

    }

    /**
     * load all profiles from the database and insert them into an array
     */
    private void loadProfiles() {
        profileList = new ArrayList<>();
        //TODO alle Profile aus der Datenbank laden und in die profileList einfügen


        Toast.makeText(this, "is leer", Toast.LENGTH_SHORT).show();

        RobotProfile robo = new RobotProfile("Robo", "127.0.0.1", 1, 2, 3, 2.0f, 3.0f, 1.0f, 4f);
        RobotProfile standardProfile = new RobotProfile("Default", "0.0.0.0", 1000, 1000, 1000, 0.5f, 0.5f, 0.6f, 4f);
        profileList.add(standardProfile);
        profileList.add(robo);


    }

    private void addProfile(RobotProfile robotProfile) {
        profileList.add(robotProfile);
        profilesAdapter.notifyDataSetChanged();
    }

    private void editProfileDialog(RobotProfile robotProfile) {
        final View profileEditView = getLayoutInflater().inflate(R.layout.profile_edit, null);
        final EditText robotName = profileEditView.findViewById(R.id.editRobotName);
        final EditText robotIp = profileEditView.findViewById(R.id.editRobotIP);
        final EditText robotControlPort = profileEditView.findViewById(R.id.editRobotControlPort);
        final EditText robotDriveMotorPort = profileEditView.findViewById(R.id.editRobotDriveMotorPort);
        final EditText robotServerMotorPort = profileEditView.findViewById(R.id.editRobotServoMotorPort);
        final EditText robotMaxX = profileEditView.findViewById(R.id.editRobotMaxX);
        final EditText robotMaxY = profileEditView.findViewById(R.id.editRobotMaxY);
        final EditText robotMaxAngularSpeed = profileEditView.findViewById(R.id.editRobotAngularVelocity);
        final EditText robotFrequency = profileEditView.findViewById(R.id.editRobotFrequency);
        Button saveProfileButton = profileEditView.findViewById(R.id.saveButton);
        Button cancelButton = profileEditView.findViewById(R.id.cancelButton);

        String portOneAsString = Integer.toString(robotProfile.getPortOne());
        String portTwoAsString = Integer.toString(robotProfile.getPortTwo());
        String portThreeAsString = Integer.toString(robotProfile.getPortThree());
        String robotMaxXAsString = Float.toString(robotProfile.getMaxX());
        String robotMaxYAsString = Float.toString(robotProfile.getMaxY());
        String robotMaxAngularSpeedAsString = Float.toString(robotProfile.getMaxAngularSpeed());
        String robotFrequencyAsString = Float.toString(robotProfile.getFrequenz());

        if (robotProfile != null) {
            Log.d("Preference", "hallo");
            robotName.setText(robotProfile.getName());
            robotIp.setText(robotProfile.getIp());
            robotControlPort.setText(portOneAsString);
            robotDriveMotorPort.setText(portTwoAsString);
            robotServerMotorPort.setText(portThreeAsString);
            robotMaxX.setText(robotMaxXAsString);
            robotMaxY.setText(robotMaxYAsString);
            robotMaxAngularSpeed.setText(robotMaxAngularSpeedAsString);
            robotFrequency.setText(robotFrequencyAsString);
        }

        final Dialog dialog = new Dialog(profileListDialog.getContext());
        dialog.setContentView(profileEditView);
        dialog.show();
        //TODO muss überarbeitet werden (darf nicht statisch sein)
        dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.7), (int) (getResources().getDisplayMetrics().heightPixels * 0.7));

        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    //Read values from TextFields
                    String name = robotName.getText().toString();
                    String ip = robotIp.getText().toString();
                    int portOne = (Integer.parseInt(robotControlPort.getText().toString()));
                    int portTwo = (Integer.parseInt(robotDriveMotorPort.getText().toString()));
                    int portThree = (Integer.parseInt(robotServerMotorPort.getText().toString()));
                    float maxAngularSpeed = (Float.parseFloat(robotMaxAngularSpeed.getText().toString()));
                    float maxX = (Float.parseFloat(robotMaxX.getText().toString()));
                    float maxY = (Float.parseFloat(robotMaxY.getText().toString()));
                    float frequency = (Float.parseFloat(robotFrequency.getText().toString()));

                    //Create a RobotProfile Object
                    RobotProfile profile = new RobotProfile(name, ip, portOne, portTwo, portThree, maxAngularSpeed, maxX, maxY, frequency);
                    dbh.updateProfile(profile);

                } catch (Exception e) {
                    Toast.makeText(SettingsActivity.this, "Please Check your input Values", Toast.LENGTH_SHORT).show();
                }

                //TODO Daten aus Dialog entnehmen und speichern
                Log.d("Preference", robotName.getText().toString());
                dialog.dismiss();

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}