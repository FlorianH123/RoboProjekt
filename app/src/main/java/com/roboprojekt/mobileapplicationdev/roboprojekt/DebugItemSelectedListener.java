package com.roboprojekt.mobileapplicationdev.roboprojekt;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

/**
 * Created by Florian on 11.01.2018.
 */

public class DebugItemSelectedListener implements AdapterView.OnItemSelectedListener {

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String debugString = parent.getItemAtPosition(pos).toString();

        if (debugString.equals("Debug1")) {
            Toast.makeText(parent.getContext(), "Debug 1 Activity" , Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(parent.getContext(), "Debug 2 Activity" , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
