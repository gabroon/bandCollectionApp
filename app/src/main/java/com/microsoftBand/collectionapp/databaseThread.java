package com.microsoftBand.collectionapp;

import android.content.Context;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Debug;
import android.util.Log;
//import android.util.Log;
import java.util.Calendar;

/**
 * Created by mohamed on 09/06/2015.
 */
public class databaseThread extends Thread {
    private Calendar c =Calendar.getInstance();
    private int day = c.get(Calendar.DAY_OF_MONTH);
    private int month =(c.get(Calendar.MONTH))+1;
    private int year = c.get(Calendar.YEAR);
    private String date=day+"-"+month+"-"+year;
    private SQLiteDatabase db;
    private volatile boolean runThread=false;
    public static DatabaseHelper databaseHelper;
    public databaseThread(DatabaseHelper databaseHelper){
    this.databaseHelper=databaseHelper;
    }

    @Override
    public void run() {

        while(runThread){


           if(MainActivity.queueForBandData != null){
              // Log.d("run thread",String.valueOf(runThread));
               if(MainActivity.queueForBandData.size() != 0){

                   BandData bandData =  MainActivity.queueForBandData.remove();
                   databaseHelper.insertInformation(databaseHelper,bandData.getAcc_x(),bandData.getAcc_y(),bandData.getAcc_z() , bandData.getGyr_x() ,bandData.getGyr_y() ,bandData.getGyr_z(),bandData.getSpeed() ,bandData.getTemp(),bandData.getHeartRate(),String.valueOf(bandData.getUvIndexLevel()),String.valueOf(bandData.getTimeStamp()),date,bandData.getLabel());
               }
           }

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
        runThread=true;
    }

    public  void stopThread(){

        runThread=false;
        Log.d("Stop rthread",String.valueOf(runThread));
    }

}
