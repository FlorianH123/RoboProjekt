package com.mobileapplicationdev.roboproject.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

    //Tabelle für eine einzige IP Adresse
    private static final String ADDR_TABLE_NAME = "ipadress";

    private static final String ID_ADDR_NAME = "id";
    private static final String ID_ADDR_TYPE = "INTEGER";

    private static final String IP_ADDR_NAME = "ip";
    private static final String IP_ADDR_TYPE = "TEXT";

    //Tabelle für ROBO
    private static final String ROBO_TABLE_NAME = "robosettings";

    private static final String ID_ROBO_NAME = "id";
    private static final String ID_ROBO_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";

    private static final String NAME_ROBO_NAME = "name";
    private static final String NAME_ROBO_TYPE = "TEXT";

    private static final String IP_ROBO_NAME = "ip";
    private static final String IP_ROBO_TYPE = "TEXT";


    private static final String ADDR_TABLE_CREATE =
            "CREATE TABLE " + ADDR_TABLE_NAME + "(" +
                    ID_ADDR_NAME + " " + ID_ADDR_TYPE + ", " +
                    IP_ADDR_NAME + " " + IP_ADDR_TYPE + ") ";


    private static final String ROBO_TABLE_CREATE =
            "CREATE TABLE " + ROBO_TABLE_NAME + "(" +
                    ID_ROBO_NAME    + " " + ID_ROBO_TYPE    + ", " +
                    NAME_ROBO_NAME + " " + NAME_ROBO_TYPE + ", " +
                    IP_ROBO_NAME + " " + IP_ROBO_TYPE + ") ";

    private static final String REGEX =
            "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])." +
            "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])." +
            "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])." +
            "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME,null , DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(ROBO_TABLE_CREATE);
        } catch(Exception ex) {
            Log.e(TAG,"Error creating table: " + ROBO_TABLE_NAME + "!", ex);
        }
        try {
            db.execSQL(ADDR_TABLE_CREATE);
        } catch(Exception ex) {
            Log.e(TAG,"Error creating table: " + ADDR_TABLE_NAME + "!", ex);
        }
    }

    /**
     * Funkitonen zur IP-Adress-Tabelle
     */

    public boolean updateIp(String ip){
        try {
            if(ip.matches(REGEX)) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();

                db.delete(ADDR_TABLE_NAME, "id = ?", new String[]{Integer.toString(0)});

                values.put(ID_ADDR_NAME, 0);
                values.put(IP_ADDR_NAME, ip);

                db.insert(ADDR_TABLE_NAME, null, values);
                return true;
            }else {
                //Keine Gültige IP
                return false;
            }
        }catch (Exception ex){
            //DatenbankError
            return false;
        }
    }

    public String getIp(){
        String ip = "0.0.0.0";
        try{
            SQLiteDatabase db = this.getReadableDatabase();

            String selectQuery = "SELECT * " +
                    " FROM " + ADDR_TABLE_NAME +
                    " WHERE " + ID_ADDR_NAME + " = 0";

            Cursor c = db.rawQuery(selectQuery, null);

            if(c != null) {
                c.moveToFirst();
                ip = c.getString(c.getColumnIndex(IP_ADDR_NAME));
            }

            return ip;
        }catch(Exception ex){
            return ip;
        }
    }


    /**
     * Default Zeug
     */

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + ROBO_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ADDR_TABLE_NAME);
        // create new tables
        onCreate(db);
    }

    // ab API-Level 11
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVer, int newVer) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + ROBO_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ADDR_TABLE_NAME);
        // create new tables
        onCreate(db);
    }

}
