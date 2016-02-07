package com.microsoftBand.collectionapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by mohamed on 02/09/2015.
 */
public class LabelsActivity extends Activity {
    EditText ETNewLabel;
    ListView labelsList;
    Button BTAddLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.label_activity);
        ETNewLabel = (EditText) findViewById(R.id.ETNewLabel);
        labelsList = (ListView) findViewById(R.id.labelsList);
        BTAddLabel =(Button) findViewById(R.id.BTAddLabel);
        loadLabels();

        BTAddLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ETNewLabel.getText().toString().equals("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(LabelsActivity.this);
                    builder.setMessage("Please write a label to add");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else{
                    MainActivity.databaseHelper.addLabel(ETNewLabel.getText().toString());
                    loadLabels();
                }

            }
        });


    }

    public void loadLabels(){
        // Spinner Drop down elements
        List<String> labels = MainActivity.databaseHelper.getLabels();
        // Creating adapter for spinner
        LabelsAdapter dataAdapter = new LabelsAdapter(this, labels);
        // attaching data adapter to spinner
        labelsList.setAdapter(dataAdapter);

    }


}
