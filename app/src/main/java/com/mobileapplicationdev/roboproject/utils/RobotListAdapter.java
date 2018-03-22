package com.mobileapplicationdev.roboproject.utils;


import android.content.Context;
import android.widget.ArrayAdapter;

import com.mobileapplicationdev.roboproject.db.DatabaseHelper;
import com.mobileapplicationdev.roboproject.models.RobotProfile;

import java.util.ArrayList;

/**
 * Created by Mesut on 23.03.2018.
 */

public class RobotListAdapter extends ArrayAdapter {
    private Context context;
    private ArrayList<RobotProfile> robotProfiles;
    private DatabaseHelper dbh;


    public RobotListAdapter(Context context, RobotProfile profile) {

        robotProfiles = new ArrayList<>();
        robotProfiles.addAll(dbh.getAllProfiles());
    }
}
