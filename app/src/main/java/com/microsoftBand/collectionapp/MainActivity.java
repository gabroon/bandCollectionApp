package com.microsoftBand.collectionapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.SampleRate;
import com.microsoft.band.sensors.HeartRateConsentListener;


public class MainActivity extends Activity implements HeartRateConsentListener{
    private BandClient client = null;
    //button to start the conncetion activity
    private Button btnStart,btnStop,btnViewTable;
    //The text view that displays the currnet status
    private TextView AccelerometerTextView;
    private TextView GyroscopeTextView;
    private TextView speedTextView;
    private TextView temperatureTextView;
    private TextView heartRateTextView;
    private EditText intervalForSaving;
    private Spinner activitiesToTrack;
    private float accelerometerX,accelerometerY,accelerometerZ =0;
    private float gyroscopeX,gyroscopeY,gyroscopeZ =0;
    private databaseThread dbThread;
    public static DatabaseHelper databaseHelper;
    private int intervalInMilliSeconds;
    private String trackedActivity;
    private PowerManager pm ;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intervalForSaving = (EditText) findViewById(R.id.seconds);
        AccelerometerTextView = (TextView) findViewById(R.id.AccelerometerStatus);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop=(Button) findViewById(R.id.btnstop);
        btnViewTable = (Button) findViewById(R.id.btnview);
        activitiesToTrack =(Spinner) findViewById(R.id.spinner);
        GyroscopeTextView = (TextView) findViewById(R.id.SecondStatusGyroscope);
        speedTextView=(TextView) findViewById(R.id.speedStatus);
        temperatureTextView = (TextView) findViewById(R.id.tempratureStatus);
        heartRateTextView = (TextView) findViewById(R.id.heartRateStatus);
        pm=(PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"wake tag");
        //this responsible of doing the database stuff
        databaseHelper=new DatabaseHelper(this);



        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wakeLock.acquire();
                Log.d("Lock","lock acquired");
                btnViewTable.setEnabled(false);
                AccelerometerTextView.setText("");
                //start an asynchronous class that check for the bands connection and connects to it if not connected
                //get activity label
                databaseThread.label=activitiesToTrack.getSelectedItem().toString();
                //get seconds
                intervalInMilliSeconds=1;
                intervalInMilliSeconds=Integer.valueOf(intervalForSaving.getText().toString()) * 1000;
                new SensorSubscription().execute();
                dbThread=new databaseThread(databaseHelper,intervalInMilliSeconds,trackedActivity);

            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbThread.stopThread();
                btnViewTable.setEnabled(true);
               //kill the connection
            if(client!=null) {
                if (client.isConnected() == true) {
                    try {
                        client.getSensorManager().unregisterAccelerometerEventListeners();
                        client.getSensorManager().unregisterDistanceEventListeners();
                        client.getSensorManager().unregisterGyroscopeEventListeners();
                        client.getSensorManager().unregisterSkinTemperatureEventListeners();
                        client.getSensorManager().unregisterHeartRateEventListeners();
                    } catch (BandIOException e) {
                        e.printStackTrace();
                    }
                    client.disconnect();

                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                        Log.d("Lock", "lock released");
                    }
                    appendToMainUI("Band Disconnected");
                }
            }
            }
        });
        btnViewTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //view table activity
                Intent viewTableActivity= new Intent("android.intent.action.VIEWINFO");
                startActivity(viewTableActivity);
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        AccelerometerTextView.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if (client != null) {
//            try {
//                //unsubscribe from the listnening events
//                client.getSensorManager().unregisterAccelerometerEventListeners();
//                client.getSensorManager().unregisterDistanceEventListeners();
//                client.getSensorManager().unregisterGyroscopeEventListeners();
//                client.getSensorManager().unregisterSkinTemperatureEventListeners();
//                client.getSensorManager().unregisterHeartRateEventListeners();
//            } catch (BandIOException e) {
//                appendToMainUI(e.getMessage());
//            }
//            dbThread.stopThread();
//        }
    }

    private class SensorSubscription extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //check fi  band is connected
                if (getConnectedBandClient()) {
                    appendToMainUI("Band is connected.\n");
                    dbThread.start();
                    client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS16);
                    client.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS16);
                    client.getSensorManager().registerDistanceEventListener(mDistanceEventListener);
                    client.getSensorManager().registerSkinTemperatureEventListener(mTemperatureEventListener);
                    if(client.getSensorManager().getCurrentHeartRateConsent() != UserConsent.GRANTED) {
                        // user has not consented, request it
                        // the calling class is both an Activity and implements
                        // HeartRateConsentListener
                        client.getSensorManager().requestHeartRateConsent(MainActivity.this, MainActivity.this);
                    }else{
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListner);
                    }
//
                } else {
                    appendToMainUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage();
                        break;
                }
                appendToMainUI(exceptionMessage);

            } catch (Exception e) {
                appendToMainUI(e.getMessage());
            }
            return null;
        }
    }

    /**
     * @param value
     * change the status text that displays errors and the accelerometer readings
     */
    private void appendToMainUI(final String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AccelerometerTextView.setText(value);
            }
        });
    }

    /**
     *
     * @param value
     *
     * update the TextView that displays the gyroscope values
     */
    private void appendToGyroscopeUI(final String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GyroscopeTextView.setText(value);
            }
        });
    }

    /**
     *
     * @param value
     *
     * update the Textview responsible of displaying the speed
     */
    private void appendToSpeedUI(final String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speedTextView.setText(value);
            }
        });
    }

    /**
     *
     * @param value
     *
     * update the TextView
     */

    private void appendToTemperature(final String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                temperatureTextView.setText(value);
            }
        });
    }

    private void appendToHeartStatus(final String value) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                heartRateTextView.setText(value);
            }
        });
    }

    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null) {
                //update UI
                appendToMainUI(String.format(" X = %.3f g\n Y = %.3f g\n Z = %.3f g", event.getAccelerationX(),
                        event.getAccelerationY(), event.getAccelerationZ()));
                //update variables in databaseThread class
                databaseThread.accelerometerX=event.getAccelerationX();
                databaseThread.accelerometerY=event.getAccelerationY();
                databaseThread.accelerometerZ=event.getAccelerationZ();

            }
        }
    };

    private BandGyroscopeEventListener mGyroscopeEventListener = new BandGyroscopeEventListener() {
        @Override
        public void onBandGyroscopeChanged(BandGyroscopeEvent bandGyroscopeEvent) {
            if(bandGyroscopeEvent != null){
                appendToGyroscopeUI(String.format(" X Gyroscope = %.3f deg/s  \n YGyroscope = %.3f deg/s\n ZGyroscope = %.3f deg/s", bandGyroscopeEvent.getAngularVelocityX(),
                        bandGyroscopeEvent.getAngularVelocityY(), bandGyroscopeEvent.getAngularVelocityZ()));
                //update variables in databaseThread class
                databaseThread.gyroscopeX=bandGyroscopeEvent.getAngularVelocityX();
                databaseThread.gyroscopeY= bandGyroscopeEvent.getAngularVelocityY();
                databaseThread.gyroscopeZ=bandGyroscopeEvent.getAngularVelocityZ();
            }
        }
    };

    private BandDistanceEventListener mDistanceEventListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(BandDistanceEvent bandDistanceEvent) {
           if(bandDistanceEvent != null){
               //change speed unit from cm/s to m/s
               float speed = bandDistanceEvent.getSpeed()/100;
               appendToSpeedUI(String.format("speed= %.3f m/s ", speed));
               //update variables in databaseThread class
               databaseThread.speed = speed;

           }
        }
    };

    private BandSkinTemperatureEventListener mTemperatureEventListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
            if(bandSkinTemperatureEvent != null){
                appendToTemperature(String.format("skin temprature= %.3f Celsius ", bandSkinTemperatureEvent.getTemperature()));
                //update variables in databaseThread class
                databaseThread.temprature=bandSkinTemperatureEvent.getTemperature();
            }
        }
    };
    private BandHeartRateEventListener mHeartRateEventListner = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
            if(bandHeartRateEvent != null){
                appendToHeartStatus(String.format("Heart Rate= %03d", bandHeartRateEvent.getHeartRate()));
                //update variables in databaseThread class
                databaseThread.heartRate=bandHeartRateEvent.getHeartRate();
            }
        }
    };


    @Override
    public void userAccepted(boolean consentGiven) {
    // handle user's heart rate consent decision
            // check current user heart rate consent
        if(consentGiven==true){
            try {
                client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListner);
            } catch (BandException e) {
                e.printStackTrace();
            }
        }else{
            client.getSensorManager().requestHeartRateConsent(this, this);
        }

    }


    /**
     *
     * @return boolean true means connected , false not coneccted
     * @throws InterruptedException
     * @throws BandException
     *
     * The method the state of the connection between the application and the band
     */
    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        //check if the band client  is null
        if (client == null) {
            //create an instance of the BandClientManger and get the devices paired to the band
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            //check the number of devices connected to the band
            if (devices.length == 0) {
                //if no devices are connected show message and return false
                appendToMainUI("Band isn't paired with your phone.\n");
                return false;
            }
            //else create a Band Client by using the BandClientManager
            //getBaseContext get current context or use this
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
            //else if the device is connected  return true
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }
        //if the band ws just created then this message will be shown to the user
        appendToMainUI("Band is connecting...\n");
        //connect the newly created client to the band
        //and compare the result to the ConnectionState.CONNECTED state
        //await() stops the thread till  the connect is finshed
        return ConnectionState.CONNECTED == client.connect().await();
    }
}
