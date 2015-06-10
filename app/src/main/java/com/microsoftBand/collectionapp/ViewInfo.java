package com.microsoftBand.collectionapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Created by mohamed on 09/06/2015.
 */
public class ViewInfo extends Activity {
    //TODO display and get info from database.
    private DatabaseHelper databaseHelper;
    private TableLayout dataTable;
    private TableRow dataRow,titleRow;
    private TextView dataCellAccX,dataCellAccY,dataCellAccZ,dataCellGyrX,dataCellGyrY,dataCellGyrZ,speed,temp,heartrate,time,date,label;
    private TextView loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewinfo);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        dataTable=(TableLayout) findViewById(R.id.dataTable);
        titleRow=(TableRow) findViewById(R.id.titleRow);
//        loading=(TextView) findViewById(R.id.loading);
        databaseHelper =MainActivity.databaseHelper;
        Cursor cursor = databaseHelper.getInformation(databaseHelper);
        //move cursor to the first row
        if(cursor.getCount()!=0) {
            cursor.moveToFirst();
            prepareTable(cursor);
        }
        titleRow.setVisibility(View.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public TableRow prepareTextView(TextView textView,TableRow dataRow,String textValue){
        textView = new TextView(this);
        textView.setBackground(getResources().getDrawable(R.drawable.cell_shape));
        textView.setPadding(2, 2, 2, 2);
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setText(textValue);
        dataRow.addView(textView);
        return dataRow;
    }

    public void prepareTable(Cursor cursor){
        do{
            dataRow = new TableRow(this);
            //accelerometer X
            prepareTextView(dataCellAccX, dataRow,cursor.getString(0));
            //accelerometer Y
            prepareTextView(dataCellAccY, dataRow,cursor.getString(1));
            //accelerometer Z
            prepareTextView(dataCellAccZ, dataRow,cursor.getString(2));
            prepareTextView(dataCellGyrX, dataRow,cursor.getString(3));
            prepareTextView(dataCellGyrY, dataRow,cursor.getString(4));
            prepareTextView(dataCellGyrZ, dataRow,cursor.getString(5));
            prepareTextView(temp, dataRow,cursor.getString(6));
            prepareTextView(heartrate, dataRow,cursor.getString(7));
            prepareTextView(speed, dataRow,cursor.getString(8));
            prepareTextView(time, dataRow,cursor.getString(9));
            prepareTextView(date, dataRow,cursor.getString(10));
            prepareTextView(label, dataRow,cursor.getString(11));
            dataTable.addView(dataRow);
        }while(cursor.moveToNext());

    }
}
