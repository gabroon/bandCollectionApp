package com.microsoftBand.collectionapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohamed on 09/06/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION=2;
    public static final String DATABASE_NAME="bandData";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DATABASE", "DATABASE Created");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS bandData(ROWID INTEGER PRIMARY KEY ,accelerometerX REAL,accelerometerY REAL,accelerometerZ REAL, gyroscopeX REAL,gyroscopeY REAL,gyroscopeZ REAL,temprature REAL, heart_rate INTEGER,speed REAL,UV TEXT,time TEXT,date TEXT,label TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS labels(ROWID INTEGER PRIMARY KEY ,label TEXT);");
        checkLabelsTable(db);
        Log.d("DATABASE", "table Created");
        //check if the UV column exists


        Log.d("DATABASE", "Table Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean checkUVColumnExist = existsColumnInTable(db,DATABASE_NAME,"UV");
        Log.d("DATABASE", String.valueOf(checkUVColumnExist));
        if(checkUVColumnExist == false){
            db.execSQL("ALTER TABLE bandData ADD COLUMN UV TEXT;");
            Log.d("DATABASE", "uv colomn added");
        }
    }

    public void insertInformation(DatabaseHelper DH,float accelerometerX,float accelerometerY,float accelerometerZ,float gyroscopeX,float gyroscopeY,float gyroscopeZ,float speed,float temprature,int heartRate,String uvLevel,String time,String date,String label){
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
        contentValues.put("UV",uvLevel);
        contentValues.put("time",time);
        contentValues.put("date",date);
        contentValues.put("label",label);
        long k =DB.insert(DATABASE_NAME,null,contentValues);
//        Log.d("DATABASE","Row inserted time");
    }

    public Cursor getInformation(DatabaseHelper databaseHelper){
        SQLiteDatabase DB = databaseHelper.getReadableDatabase();
        String[] columns = {"ROWID","accelerometerX","accelerometerY" ,"accelerometerZ", "gyroscopeX" ,"gyroscopeY" ,"gyroscopeZ" ,"temprature" , "heart_rate" ,"speed","UV","time","date","label" };
        Cursor cursor = DB.query(DATABASE_NAME,columns,null,null,null,null,null);
        return  cursor;
    }

    public List<String> getLabels(){
             SQLiteDatabase DB = getReadableDatabase();
            List<String> labels = new ArrayList<String>();
            String[] columns={"ROWID","label"};
            // Select All Query
            Cursor cursor = DB.query("labels", columns, null, null, null, null, null);
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    labels.add(cursor.getString(1));
                } while (cursor.moveToNext());
            }
            // closing connection
            cursor.close();
            DB.close();
            // returning lables
            return labels;
    }

    public void deleteAllData(DatabaseHelper databaseHelper){
        SQLiteDatabase DB = databaseHelper.getWritableDatabase();
        DB.delete(DATABASE_NAME,null,null);
    }

    public void deleteByID(DatabaseHelper databaseHelper,String ID){
        SQLiteDatabase DB = databaseHelper.getWritableDatabase();
        DB.execSQL("DELETE FROM "+DATABASE_NAME+" WHERE ROWID=" + ID);
        Log.d("database_delete", "ID =" + ID);
    }

    public boolean checkLabelsTable(SQLiteDatabase db){
        boolean empty = true;
        String count = "SELECT count(*) FROM labels";
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        if(icount >0){
            empty =false;
        }else{
            db.execSQL("INSERT INTO labels (label) VALUES('walking');");
            db.execSQL("INSERT INTO labels (label) VALUES('running');");
            db.execSQL("INSERT INTO labels (label) VALUES('standing');");
        }
        return empty;
    }

    public void addLabel(String label){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO labels(label) VALUES('"+label+"');");
    }
    public void deleteLabel(String label){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM labels WHERE label='"+label+"'");
    }

    private boolean existsColumnInTable(SQLiteDatabase inDatabase, String inTable, String columnToCheck) {
        Cursor mCursor = null;
        try {
            // Query 1 row
            mCursor = inDatabase.rawQuery("SELECT * FROM " + inTable + " LIMIT 0", null);

            // getColumnIndex() gives us the index (0 to ...) of the column - otherwise we get a -1
            if (mCursor.getColumnIndex(columnToCheck) != -1)
                return true;
            else
                return false;

        } catch (Exception Exp) {
            // Something went wrong. Missing the database? The table?
            Log.d("existsColumnInTable", "When checking whether a column exists in the table, an error occurred: " + Exp.getMessage());
            return false;
        } finally {
            if (mCursor != null) mCursor.close();
        }
    }


}
