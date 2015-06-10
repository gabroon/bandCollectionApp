package com.microsoftBand.collectionapp;

import android.content.Context;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.microsoft.band.BandClient;
import com.microsoft.band.sensors.BandAccelerometerEvent;

import java.util.Calendar;

/**
 * Created by mohamed on 09/06/2015.
 */
public class databaseThread extends Thread {
    private Long milliSeconds ;
    public volatile static float accelerometerX,accelerometerY,accelerometerZ,gyroscopeX,gyroscopeY,gyroscopeZ,temprature,speed=0;
    public volatile  static int heartRate=0;
    public volatile static String label;
    private SQLiteDatabase db;
    private Context context;
    private Boolean runThread=true;
    public static DatabaseHelper databaseHelper;
    private String activityToTrack;
    public databaseThread(DatabaseHelper databaseHelper,int Seconds,String activityToTrack){
        this.databaseHelper=databaseHelper;
        milliSeconds= Long.valueOf(Seconds);
        this.activityToTrack=activityToTrack;
//        createDatabaseAndCheck();
//        this.context=context;
//        db.openOrCreateDatabase("data",null, null);
//        db.execSQL("CREATE TABLE IF NOT EXISTS bandData(ROWID INTEGER PRIMARY KEY ,accelerometerX REAL,accelerometerY REAL,accelerometerZ REAL, gyroscopeX REAL,gyroscopeY REAL,gyroscopeZ REAL,temprature REAL, heart_rate INTEGER,speed REAL);");
    }
    @Override
    public void run() {
        super.run();
        int i = 0;
        while(runThread==true ){
            try {
                sleep(milliSeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            Log.v("ACCELEROMTER", "x=" + accelerometerX + " y=" + accelerometerY + " z=" + accelerometerZ + " ,iretation"+i );
//            Log.v("GYRSCOPE", "x=" + gyroscopeX + " y=" + gyroscopeY + " z=" + gyroscopeZ + " ,iretation"+i );
//            Log.v("Heart rate",  heartRate + " ,iretation"+i );
//            Log.v("temprature",  temprature + " ,iretation"+i );
//            Log.v("speed", speed + " ,iretation" + i);


            Calendar c =Calendar.getInstance();
            int sec = c.get(Calendar.SECOND);
            int minute =c.get(Calendar.MINUTE);
            int hour =c.get(Calendar.HOUR_OF_DAY);
            String time = hour+":"+minute+":"+sec;
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month =c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);
            String date=day+"-"+month+"-"+year;
            databaseHelper.insertInformation(databaseHelper,accelerometerX,accelerometerY,accelerometerZ , gyroscopeX ,gyroscopeY ,gyroscopeZ,speed ,temprature,heartRate,time,date,label);

            //db.execSQL("INSERT INTO bandData(accelerometerX,accelerometerY,accelerometerZ , gyroscopeX ,gyroscopeY ,gyroscopeZ ,temprature , heart_rate ,speed ) VALUES("+accelerometerX+","+accelerometerY+","+accelerometerZ+","+gyroscopeX+","+gyroscopeY+","+gyroscopeZ+","+temprature+","+heartRate+","+speed+");");
            i++;
        }
    }
    /*
    * check if the database was created if not create one
    *
    * */
    public void createDatabaseAndCheck(){
        try {
            db = SQLiteDatabase.openDatabase("data", null, SQLiteDatabase.OPEN_READONLY);
        }catch (SQLiteCantOpenDatabaseException e){
            db.openOrCreateDatabase("data",null);
        }

    }
    public  void activateThread(){
        runThread=false;
    }

    public void stopThread(){
        runThread=false;
    }

}
