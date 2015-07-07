package com.microsoftBand.collectionapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by mohamed on 09/06/2015.
 */
public class ViewInfo extends Activity {
    //TODO display and get info from database.
    private DatabaseHelper databaseHelper;
    private TableLayout dataTable;
    private TableRow dataRow, titleRow;
    private TextView dataCellAccX, dataCellAccY, dataCellAccZ, dataCellGyrX, dataCellGyrY, dataCellGyrZ, speed, temp, heartrate, time, date, label;
    private TextView loading,countOfColumns;
    private Button syncBtn,DeleteAllBtn;
    private SyncOnline syncOnline;
    private String url = "http://www.bloodbank.sd/game/band/saveData.php";
    private Context context;
    private ProgressDialog pd;
    private Cursor cursor;
    private int countOfDataSent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewinfo);
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        dataTable = (TableLayout) findViewById(R.id.dataTable);
//        titleRow = (TableRow) findViewById(R.id.titleRow);
//        loading=(TextView) findViewById(R.id.loading);
        databaseHelper = MainActivity.databaseHelper;
        syncBtn = (Button) findViewById(R.id.syncBtn);
        DeleteAllBtn = (Button) findViewById(R.id.delete);
        countOfColumns=(TextView) findViewById(R.id.count_column);
        syncOnline = new SyncOnline(url);
        context = ViewInfo.this;
        pd = new ProgressDialog(context);
        //move cursor to the first row

        cursor= databaseHelper.getInformation(databaseHelper);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            countOfColumns.setText(String.valueOf(cursor.getCount()));
            Log.d("number of rows", ""+cursor.getCount());
        }
       // new LoadTable().execute();

        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isNetworkAvailable() == true) {
                        new syncTask().execute(cursor);
                    } else {
                        String text = "No internet connection";
                        int duration = Toast.LENGTH_LONG;
                        Toast.makeText(context, text, duration).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        DeleteAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.deleteAllData(databaseHelper);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public TableRow prepareTextView(TextView textView, TableRow dataRow, String textValue) {
        textView = new TextView(this);
        textView.setBackground(getResources().getDrawable(R.drawable.cell_shape));
        textView.setPadding(2, 2, 2, 2);
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setText(textValue);
        dataRow.addView(textView);
        return dataRow;
    }

    public void prepareTable(Cursor cursor) {
        do {
            dataRow = new TableRow(this);
            //accelerometer X
            prepareTextView(dataCellAccX, dataRow, cursor.getString(1));
            //accelerometer Y
            prepareTextView(dataCellAccY, dataRow, cursor.getString(2));
            //accelerometer Z
            prepareTextView(dataCellAccZ, dataRow, cursor.getString(3));
            prepareTextView(dataCellGyrX, dataRow, cursor.getString(4));
            prepareTextView(dataCellGyrY, dataRow, cursor.getString(5));
            prepareTextView(dataCellGyrZ, dataRow, cursor.getString(6));
            prepareTextView(temp, dataRow, cursor.getString(7));
            prepareTextView(heartrate, dataRow, cursor.getString(8));
            prepareTextView(speed, dataRow, cursor.getString(9));
            prepareTextView(time, dataRow, cursor.getString(10));
            prepareTextView(date, dataRow, cursor.getString(11));
            prepareTextView(label, dataRow, cursor.getString(12));
            appendToUI(dataRow);
        } while (cursor.moveToNext());
        titleRow.setVisibility(View.VISIBLE);
    }

    public void sendDataToOnlineDatabase(Cursor cursor) {
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                syncOnline.addValuesToList("accelerometerX", cursor.getString(1));
                syncOnline.addValuesToList("accelerometerY", cursor.getString(2));
                syncOnline.addValuesToList("accelerometerZ", cursor.getString(3));
                syncOnline.addValuesToList("gyroscopeX", cursor.getString(4));
                syncOnline.addValuesToList("gyroscopeY", cursor.getString(5));
                syncOnline.addValuesToList("gyroscopeZ", cursor.getString(6));
                syncOnline.addValuesToList("temprature", cursor.getString(7));
                syncOnline.addValuesToList("heart_rate", cursor.getString(8));
                syncOnline.addValuesToList("speed", cursor.getString(9));
                syncOnline.addValuesToList("time", cursor.getString(10));
                syncOnline.addValuesToList("date", cursor.getString(11));
                syncOnline.addValuesToList("label", cursor.getString(12));
                syncOnline.postData();
                databaseHelper.deleteByID(databaseHelper, cursor.getString(cursor.getColumnIndex("ROWID")));
                Log.d("coloumn count", "" + cursor.getCount());
                pd.setProgress(countOfDataSent);
                countOfDataSent++;

            }
            while (cursor.moveToNext());
        }
    }

    public void showDataCount() {

        String text = countOfDataSent + " rows have  been sent  out of " + cursor.getCount();
        int duration = Toast.LENGTH_LONG;
        Toast.makeText(context, text, duration).show();

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
            handleProgressDialog("syncing data with cloud", "Please wait.");

        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pd != null && pd.isShowing()) {
//                hideProgressDialog();
                pd.dismiss();
                showDataCount();
            }

        }

    }

    private class LoadTable extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            prepareTable(cursor);

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            handleProgressDialog("Loading table", "Pleas wait ...");

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pd != null) {
                hideProgressDialog();

            }

        }

    }

    public boolean isNetworkAvailable() throws IOException {
        int timeout = 3000;
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
//            if (InetAddress.getByName("google.com").isReachable(timeout)) {
            return true;
//            } else {
//                return false;
//            }
        } else {
            return false;
        }
    }

    private void appendToUI(final TableRow dataRow) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataTable.addView(dataRow);
            }
        });
    }

    private void handleProgressDialog(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pd.setTitle(title);
                pd.setCancelable(false);
                pd.setIndeterminate(false);
                pd.setProgressStyle(pd.STYLE_HORIZONTAL);
                pd.setProgress(0);
                pd.setMax(cursor.getCount());
                pd.show();
            }
        });
    }


    private void hideProgressDialog() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pd.dismiss();
                pd.cancel();
            }
        });
    }
}
