package com.microsoftBand.collectionapp;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by mohamed on 09/06/2015.
 */
public class ViewInfo extends Activity {
    //TODO display and get info from database.
    //databaseHelper ahs all the functions that assists with the database functionalties
    private DatabaseHelper databaseHelper;
    //this variable holds the number of rows with the applications database
    private TextView countOfRows;
    //buttons to delete and export data to csv
    private Button syncBtn,SensorsBtn,DeleteAllBtn,manageLabels,aboutBTN;

    //
    //
    // private SyncOnline syncOnline;
    //the edit text used to extract the name of the csv file
    private EditText csvFileNameEditText;
    //this variable will hold the string representation of the csv files name
    private String csvFileName;

    private String url = "http://www.bloodbank.sd/game/band/saveData.php";

    private Context context;

    //this varaible will hold the progress dialog used to the display the progress of exporting the data base values to a csv file
    private ProgressDialog pd;
    //will the database's cursor
    private Cursor cursor;
    //this variable will keep track of how many rows of data have been sent
    private int countOfDataSent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set all the interface components and initialize the variables
        setContentView(R.layout.viewinfo);
        databaseHelper = MainActivity.databaseHelper;
        syncBtn = (Button) findViewById(R.id.syncBtn);
        DeleteAllBtn = (Button) findViewById(R.id.delete);
        manageLabels = (Button) findViewById(R.id.manageLabel);
        aboutBTN = (Button) findViewById(R.id.About);
        SensorsBtn =(Button) findViewById(R.id.Sensors);
        countOfRows=(TextView) findViewById(R.id.count_column);
        csvFileNameEditText=(EditText) findViewById(R.id.csvFile);
       // syncOnline = new SyncOnline(url);
        context = ViewInfo.this;
        pd = new ProgressDialog(context);
        //get the cursur from the current database
        cursor= databaseHelper.getInformation(databaseHelper);
        //cehck if database is not empty
        if (cursor.getCount() != 0) {
            //move the cursor to the first row
            cursor.moveToFirst();
            //get the number of rows in the database
            countOfRows.setText(String.valueOf(cursor.getCount()));
            Log.d("number of rows", ""+cursor.getCount());
        }
       // new LoadTable().execute();

        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        //start the exporting task
                 csvFileName = csvFileNameEditText.getText().toString();
                if(csvFileName.trim().equals("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewInfo.this);
                    builder.setMessage("please write a name for the file");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else {
                    new syncTask().execute(cursor);
                }

            }
        }
        );

        DeleteAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewInfo.this);
                builder.setMessage(R.string.delete_prompt_message).setTitle(R.string.delete_prompt_title);
                builder.setPositiveButton(R.string.delete_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        databaseHelper.deleteAllData(databaseHelper);
                        String text = "All data Deleted";
                        showToastMessage(text);
                    }
                });
                builder.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
        SensorsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent sensorsPage= new Intent("android.intent.action.SENSORSPAGE");
                startActivity(sensorsPage);
            }
        });

        manageLabels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent manageLabelsAvtivity= new Intent("android.intent.action.MANAGELABELS");
                startActivity(manageLabelsAvtivity);

            }
        });
        aboutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aboutActivity= new Intent("android.intent.action.ABOUT");
                startActivity(aboutActivity);
            }
        });
    }


    public void copyToCsv(Cursor cursor) {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, csvFileName+".csv");
        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            csvWrite.writeNext(cursor.getColumnNames());
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                do{
                    //Which column you want to exprort
                    String arrStr[] = {cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getString(10), cursor.getString(11), cursor.getString(12),cursor.getString(13)};
                    csvWrite.writeNext(arrStr);
                    Log.d("coloumn count", "" + cursor.getCount());
//                    databaseHelper.deleteByID(databaseHelper, cursor.getString(cursor.getColumnIndex("ROWID")));
                    pd.setProgress(countOfDataSent);
                    countOfDataSent++;
                }while (cursor.moveToNext());
                csvWrite.close();
                cursor.close();
            }
            Log.d("CSV","DONE");
        }
        catch(Exception sqlEx)
        {
            Log.e("ViewInfo", sqlEx.getMessage(), sqlEx);
        }

    }



//
//    public void sendDataToOnlineDatabase(Cursor cursor) {
//        if (cursor.getCount() != 0) {
//            cursor.moveToFirst();
//            do {
//                syncOnline.addValuesToList("accelerometerX", cursor.getString(1));
//                syncOnline.addValuesToList("accelerometerY", cursor.getString(2));
//                syncOnline.addValuesToList("accelerometerZ", cursor.getString(3));
//                syncOnline.addValuesToList("gyroscopeX", cursor.getString(4));
//                syncOnline.addValuesToList("gyroscopeY", cursor.getString(5));
//                syncOnline.addValuesToList("gyroscopeZ", cursor.getString(6));
//                syncOnline.addValuesToList("temprature", cursor.getString(7));
//                syncOnline.addValuesToList("heart_rate", cursor.getString(8));
//                syncOnline.addValuesToList("speed", cursor.getString(9));
//                syncOnline.addValuesToList("time", cursor.getString(10));
//                syncOnline.addValuesToList("date", cursor.getString(11));
//                syncOnline.addValuesToList("label", cursor.getString(12));
//                syncOnline.postData();
//                databaseHelper.deleteByID(databaseHelper, cursor.getString(cursor.getColumnIndex("ROWID")));
//                Log.d("coloumn count", "" + cursor.getCount());
//                pd.setProgress(countOfDataSent);
//                countOfDataSent++;
//
//            }
//            while (cursor.moveToNext());
//        }
//    }

    public void showToastMessage(String message) {
        int duration = Toast.LENGTH_LONG;
        Toast.makeText(context, message, duration).show();

    }

    private class syncTask extends AsyncTask<Cursor, Void, Void> {

        @Override
        protected Void doInBackground(Cursor... cursor) {

            copyToCsv(cursor[0]);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            handleProgressDialog("Exporting rows to csv file", "Please wait.");

        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pd != null && pd.isShowing()) {
//                hideProgressDialog();
                pd.dismiss();
                String text = countOfDataSent + " rows have  been sent  out of " + cursor.getCount();
                showToastMessage(text);
            }

        }

    }


//    public boolean isNetworkAvailable() throws IOException {
//        int timeout = 3000;
//        ConnectivityManager cm =
//                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = cm.getActiveNetworkInfo();
//
//        if (netInfo != null && netInfo.isConnected()) {
////            if (InetAddress.getByName("google.com").isReachable(timeout)) {
//            return true;
////            } else {
////                return false;
////            }
//        } else {
//            return false;
//        }
//    }



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
