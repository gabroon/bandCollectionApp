package com.microsoftBand.collectionapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
    private Button syncBtn;
    private SyncOnline syncOnline;
    private String url="http://datacollect.comule.com/saveData.php";
    private Context context;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewinfo);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        dataTable=(TableLayout) findViewById(R.id.dataTable);
        titleRow=(TableRow) findViewById(R.id.titleRow);
//        loading=(TextView) findViewById(R.id.loading);
        databaseHelper =MainActivity.databaseHelper;
        syncBtn=(Button)findViewById(R.id.syncBtn);
        syncOnline=new SyncOnline(url);
        context=this;
        final Cursor cursor = databaseHelper.getInformation(databaseHelper);
        //move cursor to the first row
        if(cursor.getCount()!=0) {
            cursor.moveToFirst();
            prepareTable(cursor);
        }
        titleRow.setVisibility(View.VISIBLE);

        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToOnlineDatabase(cursor);
            }
        });
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
            prepareTextView(dataCellAccX, dataRow, cursor.getString(0));
            //accelerometer Y
            prepareTextView(dataCellAccY, dataRow, cursor.getString(1));
            //accelerometer Z
            prepareTextView(dataCellAccZ, dataRow, cursor.getString(2));
            prepareTextView(dataCellGyrX, dataRow, cursor.getString(3));
            prepareTextView(dataCellGyrY, dataRow, cursor.getString(4));
            prepareTextView(dataCellGyrZ, dataRow, cursor.getString(5));
            prepareTextView(temp, dataRow, cursor.getString(6));
            prepareTextView(heartrate, dataRow, cursor.getString(7));
            prepareTextView(speed, dataRow, cursor.getString(8));
            prepareTextView(time, dataRow, cursor.getString(9));
            prepareTextView(date, dataRow, cursor.getString(10));
            prepareTextView(label, dataRow, cursor.getString(11));
            dataTable.addView(dataRow);
        }while(cursor.moveToNext());

    }

    public void sendDataToOnlineDatabase(Cursor cursor){
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
        do {
                syncOnline.addValuesToList("accelerometerX", cursor.getString(0));
                syncOnline.addValuesToList("accelerometerY", cursor.getString(1));
                syncOnline.addValuesToList("accelerometerZ", cursor.getString(2));
                syncOnline.addValuesToList("gyroscopeX", cursor.getString(3));
                syncOnline.addValuesToList("gyroscopeY", cursor.getString(4));
                syncOnline.addValuesToList("gyroscopeZ", cursor.getString(5));
                syncOnline.addValuesToList("temprature", cursor.getString(6));
                syncOnline.addValuesToList("heart_rate", cursor.getString(7));
                syncOnline.addValuesToList("speed", cursor.getString(8));
                syncOnline.addValuesToList("time", cursor.getString(9));
                syncOnline.addValuesToList("date", cursor.getString(10));
                syncOnline.addValuesToList("label", cursor.getString(11));
                syncOnline.postData();
            }
            while (cursor.moveToNext()) ;
        }
    }

    public void deleteDataFromDatabase(){
        databaseHelper.deleteAllData(databaseHelper);
    }

    private class syncTask extends AsyncTask<Cursor, Void, Void> {

        @Override
        protected Void doInBackground(Cursor... cursor) {
            sendDataToOnlineDatabase(cursor[0]);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDialog pd = new  ProgressDialog(context);
            pd.setTitle("syncing data with cloud");
            pd.setMessage("Please wait.");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pd!=null) {
                pd.dismiss();
            }

        }

    }
}
