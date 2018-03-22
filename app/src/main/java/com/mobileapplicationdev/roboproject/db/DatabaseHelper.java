package com.mobileapplicationdev.roboproject.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mobileapplicationdev.roboproject.models.RobotProfile;

import java.util.ArrayList;

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

    //Tabelle für Profile
    private static final String PRO_TABLE_NAME = "profiles";

    private static final String ID_PRO_NAME = "id";
    private static final String ID_PRO_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";

    private static final String NAME_PRO_NAME = "name";
    private static final String NAME_PRO_TYPE = "TEXT";

    private static final String IP_PRO_NAME = "ip";
    private static final String IP_PRO_TYPE = "TEXT";

    private static final String PORT_1_PRO_NAME = "portOne";
    private static final String PORT_1_PRO_TYPE = "INTEGER";

    private static final String PORT_2_PRO_NAME = "portTwo";
    private static final String PORT_2_PRO_TYPE = "INTEGER";

    private static final String PORT_3_PRO_NAME = "portThree";
    private static final String PORT_3_PRO_TYPE = "INTEGER";

    private static final String MAX_ANG_PRO_NAME = "maxAngularSpeed";
    private static final String MAX_ANG_PRO_TYPE = "FLOAT";

    private static final String MAX_X_PRO_NAME = "maxX";
    private static final String MAX_X_PRO_TYPE = "FLOAT";

    private static final String MAX_Y_PRO_NAME = "maxY";
    private static final String MAX_Y_PRO_TYPE = "FLOAT";

    private static final String FREQ_PRO_NAME = "frequenz";
    private static final String FREQ_PRO_TYPE = "FLOAT";


    private static final String PRO_TABLE_CREATE =
            "CREATE TABLE " + PRO_TABLE_NAME + "(" +
                    ID_PRO_NAME + " " + ID_PRO_TYPE + ", " +
                    NAME_PRO_NAME + " " + NAME_PRO_TYPE + ", " +
                    IP_PRO_NAME + " " + IP_PRO_TYPE + ", " +
                    PORT_1_PRO_NAME + " " + PORT_1_PRO_TYPE + ", " +
                    PORT_2_PRO_NAME + " " + PORT_2_PRO_TYPE + ", " +
                    PORT_3_PRO_NAME + " " + PORT_3_PRO_TYPE + ", " +
                    MAX_ANG_PRO_NAME + " " + MAX_ANG_PRO_TYPE + ", " +
                    MAX_X_PRO_NAME + " " + MAX_X_PRO_TYPE + ", " +
                    MAX_Y_PRO_NAME + " " + MAX_Y_PRO_TYPE + ", " +
                    FREQ_PRO_NAME + " " + FREQ_PRO_TYPE + ")";

    private static final String ADDR_TABLE_CREATE =
            "CREATE TABLE " + ADDR_TABLE_NAME + "(" +
                    ID_ADDR_NAME + " " + ID_ADDR_TYPE + ", " +
                    IP_ADDR_NAME + " " + IP_ADDR_TYPE + ") ";

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
            db.execSQL(PRO_TABLE_CREATE);
        } catch(Exception ex) {
            Log.e(TAG,"Error creating table: " + PRO_TABLE_NAME + "!", ex);
        }
        try {
            db.execSQL(ADDR_TABLE_CREATE);
        } catch(Exception ex) {
            Log.e(TAG,"Error creating table: " + ADDR_TABLE_NAME + "!", ex);
        }
    }

    /**
     * Funktionen für Profil-Tabelle
     */
    public boolean insertProfile(RobotProfile profile){
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(NAME_PRO_NAME, profile.getName());
            values.put(IP_PRO_NAME, profile.getIp());
            values.put(PORT_1_PRO_NAME, profile.getPortOne());
            values.put(PORT_2_PRO_NAME, profile.getPortTwo());
            values.put(PORT_3_PRO_NAME, profile.getPortThree());
            values.put(MAX_ANG_PRO_NAME, profile.getMaxAngularSpeed());
            values.put(MAX_X_PRO_NAME, profile.getMaxX());
            values.put(MAX_Y_PRO_NAME, profile.getMaxY());
            values.put(FREQ_PRO_NAME, profile.getFrequenz());

            db.insert(PRO_TABLE_NAME, null, values);

            return true;
        }catch(Exception ex){
            Log.e(TAG, "Couldn't insert Profile: " + ex);
            return false;
        }
    }

    public boolean updateProfile(RobotProfile profile){
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(NAME_PRO_NAME, profile.getName());
            values.put(IP_PRO_NAME, profile.getIp());
            values.put(PORT_1_PRO_NAME, profile.getPortOne());
            values.put(PORT_2_PRO_NAME, profile.getPortTwo());
            values.put(PORT_3_PRO_NAME, profile.getPortThree());
            values.put(MAX_ANG_PRO_NAME, profile.getMaxAngularSpeed());
            values.put(MAX_X_PRO_NAME, profile.getMaxX());
            values.put(MAX_Y_PRO_NAME, profile.getMaxY());
            values.put(FREQ_PRO_NAME, profile.getFrequenz());

            db.update(PRO_TABLE_NAME, values, "id = ?", new String[]{String.valueOf(profile.getId())});
            return true;
        }catch (Exception ex){
            Log.e(TAG, "Couldn't update profile" + ex);
            return false;

        }
    }

    public ArrayList<RobotProfile> getAllProfiles(){
        try{
            ArrayList<RobotProfile> profiles = new ArrayList<RobotProfile>();
            String selectQuery = "SELECT * FROM " + PRO_TABLE_NAME;

            Log.d(TAG, selectQuery);

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);

            if(c.moveToFirst()){
                do {
                    RobotProfile profile = new RobotProfile();

                    profile.setId(c.getInt(c.getColumnIndex(ID_PRO_NAME)));
                    profile.setName(c.getString(c.getColumnIndex(NAME_PRO_NAME)));
                    profile.setIp(c.getString(c.getColumnIndex(IP_PRO_NAME)));
                    profile.setPortOne(c.getInt(c.getColumnIndex(PORT_1_PRO_NAME)));
                    profile.setPortTwo(c.getInt(c.getColumnIndex(PORT_2_PRO_NAME)));
                    profile.setPortThree(c.getInt(c.getColumnIndex(PORT_3_PRO_NAME)));
                    profile.setMaxAngularSpeed(c.getFloat(c.getColumnIndex(MAX_ANG_PRO_NAME)));
                    profile.setMaxX(c.getFloat(c.getColumnIndex(MAX_X_PRO_NAME)));
                    profile.setMaxY(c.getFloat(c.getColumnIndex(MAX_Y_PRO_NAME)));
                    profile.setFrequenz(c.getFloat(c.getColumnIndex(FREQ_PRO_NAME)));

                    profiles.add(profile);

                }while(c.moveToNext());
            }

            return profiles;
        }catch (Exception ex){
            Log.e(TAG, "Couldn't get all Profiles.\n" + ex);
            return null;
        }
    }

    public boolean deleteProfile(Integer id){
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(PRO_TABLE_NAME, "id = ?" , new String[]{Integer.toString(id)});
            return true;
        }catch(Exception ex){
            Log.e(TAG, "Couldn't delete profile with id=" + id);
            return false;
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
        db.execSQL("DROP TABLE IF EXISTS " + PRO_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ADDR_TABLE_NAME);
        // create new tables
        onCreate(db);
    }

    // ab API-Level 11
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVer, int newVer) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + PRO_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ADDR_TABLE_NAME);
        // create new tables
        onCreate(db);
    }

}
