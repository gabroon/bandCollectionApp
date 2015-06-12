package com.microsoftBand.collectionapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by mohamed on 09/06/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION=1;
    public static final String DATABASE_NAME="bandData";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DATABASE", "DATABASE Created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS bandData(ROWID INTEGER PRIMARY KEY ,accelerometerX REAL,accelerometerY REAL,accelerometerZ REAL, gyroscopeX REAL,gyroscopeY REAL,gyroscopeZ REAL,temprature REAL, heart_rate INTEGER,speed REAL,time TEXT,date TEXT,label TEXT);");
        Log.d("DATABASE", "Table Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertInformation(DatabaseHelper DH,float accelerometerX,float accelerometerY,float accelerometerZ,float gyroscopeX,float gyroscopeY,float gyroscopeZ,float speed,float temprature,int heartRate,String time,String date,String label){
        SQLiteDatabase DB = DH.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("accelerometerX",accelerometerX);
        contentValues.put("accelerometerY",accelerometerY);
        contentValues.put("accelerometerZ",accelerometerZ);
        contentValues.put("gyroscopeX",gyroscopeX);
        contentValues.put("gyroscopeY",gyroscopeY);
        contentValues.put("gyroscopeZ",gyroscopeZ);
        contentValues.put("heart_rate",heartRate);
        contentValues.put("speed",speed);
        contentValues.put("temprature",temprature);
        contentValues.put("time",time);
        contentValues.put("date",date);
        contentValues.put("label",label);
        long k =DB.insert(DATABASE_NAME,null,contentValues);
        Log.d("DATABASE","Row inserted");

    }

    public Cursor getInformation(DatabaseHelper databaseHelper){
       SQLiteDatabase DB = databaseHelper.getReadableDatabase();
        String[] columns = {"accelerometerX","accelerometerY" ,"accelerometerZ", "gyroscopeX" ,"gyroscopeY" ,"gyroscopeZ" ,"temprature" , "heart_rate" ,"speed","time","date","label" };
        Cursor cursor = DB.query(DATABASE_NAME,columns,null,null,null,null,null);
        return  cursor;
    }

    public void deleteAllData(DatabaseHelper databaseHelper){
        SQLiteDatabase DB = databaseHelper.getWritableDatabase();
        DB.delete(DATABASE_NAME,null,null);
    }
}
