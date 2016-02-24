package com.microsoftBand.collectionapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Created by mohamed on 15/09/2015.
 */
public class SensorsPage extends Activity {
    private CheckBox acc_checkbox,gyr_checkbox,heart_checkbox,skintemp_checkbox,uv_checkbox,speed_checkbox;
    public volatile static boolean gyro_flag=true,heart_flag=true,skin_flag=true,acc_flag=true,uv_flag=true,speed_flag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensorspage);
        acc_checkbox =(CheckBox)findViewById(R.id.acc_Ch_box);
        acc_checkbox.setEnabled(false);
        gyr_checkbox=(CheckBox) findViewById(R.id.gyroscope_Ch_box);

        if(gyro_flag==true){
            gyr_checkbox.setChecked(true);
        }

        heart_checkbox=(CheckBox) findViewById(R.id.Heart_Ch_box);
        if(heart_flag==true){
            heart_checkbox.setChecked(true);
        }

        skintemp_checkbox =(CheckBox) findViewById(R.id.skin_temp_Ch_box);
        if(skin_flag==true){
            skintemp_checkbox.setChecked(true);
        }
        uv_checkbox = (CheckBox) findViewById(R.id.UV);
        if(uv_flag==true){
            uv_checkbox.setChecked(true);
        }
        speed_checkbox = (CheckBox) findViewById(R.id.speed);
        if(speed_flag==true){
            speed_checkbox.setChecked(true);
        }

        acc_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    acc_flag=true;
                }else{
                    acc_flag=false;
                }
            }
        });

        gyr_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    gyro_flag=true;
                }else{
                    gyro_flag=false;
                }
                Log.d("Gyro flag",String.valueOf(gyro_flag));
            }
        });

        heart_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    heart_flag=true;
                }else{
                    heart_flag=false;
                }
                Log.d("heart flag",String.valueOf(heart_flag));
            }
        });

        skintemp_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    skin_flag = true;
                } else {
                    skin_flag = false;
                }
                Log.d("skin flag",String.valueOf(skin_flag));
            }
        });

        uv_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    uv_flag = true;
                } else {
                    uv_flag = false;
                }
                Log.d("UV flag ",String.valueOf(uv_flag));
            }
        });

        speed_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    speed_flag = true;
                } else {
                    speed_flag = false;
                }
                Log.d("speed flag ",String.valueOf(speed_flag));
            }
        });

    }
}
