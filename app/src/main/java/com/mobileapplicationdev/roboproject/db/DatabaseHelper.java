package com.mobileapplicationdev.roboproject.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Janik on 31.01.2018.
 *
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "RoboController.db";
    private static final int DATABASE_VERSION = 1;

    //Tabelle für ROBO
    private static final String ROBO_TABLE_NAME = "robosettings";

    private static final String ID_ROBO_NAME = "id";
    private static final String ID_ROBO_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";

    private static final String NAME_ROBO_NAME = "name";
    private static final String NAME_ROBO_TYPE = "TEXT";

    private static final String IP_ROBO_NAME = "ip";
    private static final String IP_ROBO_TYPE = "TEXT";


    private static final String ROBO_TABLE_CREATE =
            "CREATE TABLE " + ROBO_TABLE_NAME + "(" +
                    ID_ROBO_NAME    + " " + ID_ROBO_TYPE    + ", " +
                    NAME_ROBO_NAME + " " + NAME_ROBO_TYPE + ", " +
                    IP_ROBO_NAME + " " + IP_ROBO_TYPE + ") ";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME,null , DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(ROBO_TABLE_CREATE);
        } catch(SQLException ex) {
            Log.e(TAG,"Error creating table: robosettings!", ex);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + ROBO_TABLE_NAME);
        // create new tables
        onCreate(db);
    }

    // ab API-Level 11
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVer, int newVer) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + ROBO_TABLE_NAME);
        // create new tables
        onCreate(db);
    }

}
