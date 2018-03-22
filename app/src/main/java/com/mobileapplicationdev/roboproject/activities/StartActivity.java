package com.mobileapplicationdev.roboproject.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.mobileapplicationdev.roboproject.R;
import com.mobileapplicationdev.roboproject.db.DatabaseHelper;
import com.mobileapplicationdev.roboproject.models.RobotProfile;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity {

    private ListView listView = null;
    private Button skipButton = null;

    private DatabaseHelper dbh = null;
    private ArrayList<RobotProfile> profiles = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        dbh = new DatabaseHelper(this);
        profiles = dbh.getAllProfiles();
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.skip_button:
                startMainActivity();
                break;
            default:
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
        }
    }

    public void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}
